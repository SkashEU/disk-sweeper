use crate::models::{DiskStats, FileItem, ScanResponse, ScanState};
use rayon::prelude::*;
use std::fs;
use std::path::Path;
use std::sync::Arc;
use std::sync::atomic::Ordering;

use walkdir::WalkDir;

pub fn scan_root(path_str: &str, state: &Arc<ScanState>) -> ScanResponse {
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
            eprintln!("RUST ERROR: Cannot read root directory '{}': {}", path_str, e);
            return ScanResponse {
                stats: DiskStats {
                    drive_total_bytes: 0, drive_free_bytes: 0, drive_used_bytes: 0,
                    scanned_file_count: 0, scanned_total_bytes: 0
                },
                files: Vec::new()
            };
        }
    };

    let mut results: Vec<FileItem> = entries
        .into_par_iter()
        .filter_map(|entry| {
            let entry = entry.ok()?;
            let meta = entry.metadata().ok()?;
            let full_path = entry.path().to_string_lossy().to_string();
            let name = entry.file_name().to_string_lossy().to_string();
            let is_dir = meta.is_dir();

            let size = if is_dir {
                calculate_deep_size(&full_path, state)
            } else {
                let s = meta.len();
                state.add_file(s);
                s
            };

            Some(FileItem { name, path: full_path, is_dir, size_bytes: size })
        })
        .collect();

    results.sort_unstable_by(|a, b| b.size_bytes.cmp(&a.size_bytes));
    let total_space = fs4::total_space(path).unwrap_or(0);
    let free_space = fs4::available_space(path).unwrap_or(0);
    let used_space = total_space.saturating_sub(free_space);

    ScanResponse {
        stats: DiskStats {
            drive_total_bytes: total_space,
            drive_free_bytes: free_space,
            drive_used_bytes: used_space,
            scanned_file_count: state.scanned_count.load(Ordering::Relaxed),
            scanned_total_bytes: state.scanned_bytes.load(Ordering::Relaxed),
        },
        files: results
    }
}

fn calculate_deep_size(path: &str, state: &Arc<ScanState>) -> u64 {
    let mut total_size = 0;
    let mut batch_size = 0;
    let mut batch_count = 0;

    for (index, entry) in WalkDir::new(path)
        .follow_links(false)
        .same_file_system(true)
        .into_iter()
        .enumerate()
    {
        match entry {
            Ok(e) => {
                if index % 1000 == 0 {
                    state.try_update_path(&e.path().to_string_lossy());

                    if batch_count > 0 {
                        state.add_batch(batch_size, batch_count);
                        batch_size = 0;
                        batch_count = 0;
                    }
                }

                match e.metadata() {
                    Ok(m) if m.is_file() => {
                        let len = m.len();
                        total_size += len;
                        batch_size += len;
                        batch_count += 1;
                    },
                    _ => {}
                }
            },
            Err(error) => {
                eprintln!("ACCESS DENIED: {}", error);
            }
        }
    }

    if batch_count > 0 {
        state.add_batch(batch_size, batch_count);
    }

    total_size
}