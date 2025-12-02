use crate::models::{StorageStats, NativeFileSystemEntry, ScanResult, ScanState};
use rayon::prelude::*;
use std::fs;
use std::os::unix::fs::MetadataExt;
use std::path::Path;
use std::sync::Arc;
use std::sync::atomic::Ordering;
use std::time::{Duration, Instant};
use walkdir::WalkDir;

pub fn scan_root(path_str: &str, state: &Arc<ScanState>) -> ScanResult {
    let path = Path::new(path_str);

    if let Ok(total) = fs4::total_space(path) {
        let free = fs4::available_space(path).unwrap_or(0);
        let used = total.saturating_sub(free);
        state.target_bytes.store(used, Ordering::Relaxed);
        state.drive_capacity.store(total, Ordering::Relaxed);
    } else if let Ok(metadata) = fs::metadata(path) {
        state.target_bytes.store(metadata.len(), Ordering::Relaxed);
    }

    let entries = match fs::read_dir(path) {
        Ok(e) => e.collect::<Vec<_>>(),
        Err(e) => {
            eprintln!(
                "RUST ERROR: Cannot read root directory '{}': {}",
                path_str, e
            );
            return ScanResult {
                stats: StorageStats {
                    drive_total_bytes: 0,
                    drive_free_bytes: 0,
                    drive_used_bytes: 0,
                    scanned_file_count: 0,
                    scanned_total_bytes: 0,
                },
                files: Vec::new(),
            };
        }
    };

    let mut results: Vec<NativeFileSystemEntry> = entries
        .into_iter()
        .par_bridge()
        .filter_map(|entry| {
            let entry = entry.ok()?;
            let meta = entry.metadata().ok()?;
            let full_path = entry.path().to_string_lossy().to_string();
            let name = entry.file_name().to_string_lossy().to_string();
            let is_dir = meta.is_dir();

            #[cfg(unix)]
            use std::os::unix::fs::MetadataExt;
            #[cfg(unix)]
            let id = meta.ino();
            #[cfg(not(unix))]
            let id = {
                let mut hasher = DefaultHasher::new();
                full_path.hash(&mut hasher);
                hasher.finish()
            };

            let (size, allocated_size) = if is_dir {
                calculate_deep_size(&full_path, state)
            } else {
                let s = meta.len();

                // OPTIMIZATION: Update stats for top-level files immediately
                state.add_file(s);

                #[cfg(unix)]
                let a = meta.blocks() * 512;
                #[cfg(not(unix))]
                let a = s;
                (s, a)
            };

            Some(NativeFileSystemEntry {
                id,
                name,
                path: full_path,
                is_dir,
                size_bytes: size,
                allocated_size_bytes: allocated_size,
            })
        })
        .collect();

    results.sort_unstable_by(|a, b| b.size_bytes.cmp(&a.size_bytes));

    let total_space = fs4::total_space(path).unwrap_or(0);
    let free_space = fs4::available_space(path).unwrap_or(0);

    ScanResult {
        stats: StorageStats {
            drive_total_bytes: total_space,
            drive_free_bytes: free_space,
            drive_used_bytes: total_space.saturating_sub(free_space),
            scanned_file_count: state.scanned_count.load(Ordering::Relaxed),
            scanned_total_bytes: state.scanned_bytes.load(Ordering::Relaxed),
        },
        files: results,
    }
}

fn calculate_deep_size(path: &str, state: &Arc<ScanState>) -> (u64, u64) {
    let mut total_size = 0;
    let mut total_allocated = 0;

    let mut batch_size = 0u64;
    let mut batch_count = 0u64;

    let mut last_data_flush = Instant::now();
    let mut last_path_update = Instant::now();

    let data_interval = Duration::from_millis(30);
    let visual_interval = Duration::from_millis(150);

    for entry in WalkDir::new(path)
        .follow_links(false)
        .same_file_system(true)
        .into_iter()
        .filter_map(|e| e.ok())
    {
        if let Ok(m) = entry.metadata() {
            if m.is_file() {
                let len = m.len();
                total_size += len;
                batch_size += len;
                batch_count += 1;

                #[cfg(unix)]
                {
                    use std::os::unix::fs::MetadataExt;
                    total_allocated += m.blocks() * 512;
                }
                #[cfg(not(unix))]
                {
                    total_allocated += len;
                }
            }
        }

        if last_data_flush.elapsed() > data_interval {
            if batch_count > 0 {
                state.add_batch(batch_size, batch_count);
                batch_size = 0;
                batch_count = 0;
            }
            last_data_flush = Instant::now();
            if last_path_update.elapsed() > visual_interval {
                state.try_update_path(&entry.path().to_string_lossy());
                last_path_update = Instant::now();
            }
        }
    }

    if batch_count > 0 {
        state.add_batch(batch_size, batch_count);
    }

    (total_size, total_allocated)
}