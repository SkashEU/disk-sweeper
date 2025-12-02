use crate::models::{NativeFileSystemEntry, ScanResult, ScanState, StorageStats};
use rayon::prelude::*;
use std::fs;
use std::hash::{DefaultHasher, Hash, Hasher};
use std::path::Path;
use std::sync::atomic::Ordering;
use std::sync::Arc;

pub fn scan_root_recursive(path_str: &str, state: &Arc<ScanState>) -> ScanResult {
    let path = Path::new(path_str);

    if let Ok(total) = fs4::total_space(path) {
        let free = fs4::available_space(path).unwrap_or(0);
        let used = total.saturating_sub(free);

        state.drive_capacity.store(total, Ordering::Relaxed);
        state.target_bytes.store(used, Ordering::Relaxed);
    }

    let _ = process_directory(path, state);

    let root_files = state.dir_cache
        .get(path_str)
        .map(|v| v.clone())
        .unwrap_or_default();

    let final_scanned = state.scanned_bytes.load(Ordering::Relaxed);
    state.target_bytes.store(final_scanned, Ordering::Relaxed);

    let total_space = state.drive_capacity.load(Ordering::Relaxed);
    let free_space = fs4::available_space(path).unwrap_or(0);

    ScanResult {
        stats: StorageStats {
            drive_total_bytes: total_space,
            drive_free_bytes: free_space,
            drive_used_bytes: total_space.saturating_sub(free_space),
            scanned_file_count: state.scanned_count.load(Ordering::Relaxed),
            scanned_total_bytes: final_scanned,
        },
        files: root_files,
    }
}

fn process_directory(path: &Path, state: &Arc<ScanState>) -> NativeFileSystemEntry {
    let path_str = path.to_string_lossy().to_string();
    let name = path.file_name().unwrap_or_default().to_string_lossy().to_string();

    state.try_update_path(&path_str);

    let entries: Vec<_> = match fs::read_dir(path) {
        Ok(read_dir) => read_dir.filter_map(|e| e.ok()).collect(),
        Err(_) => Vec::new(),
    };

    let mut children: Vec<NativeFileSystemEntry> = entries
        .par_iter()
        .map(|entry| {
            let meta_res = entry.metadata();
            if meta_res.is_err() { return None; }
            let meta = meta_res.unwrap();

            let child_path = entry.path();
            let is_dir = meta.is_dir();

            if is_dir {
                Some(process_directory(&child_path, state))
            } else {
                let size = meta.len();
                state.add_file(size);

                #[cfg(unix)]
                use std::os::unix::fs::MetadataExt;
                #[cfg(unix)]
                let allocated = meta.blocks() * 512;
                #[cfg(not(unix))]
                let allocated = size;

                let id = {
                    let mut hasher = DefaultHasher::new();
                    child_path.to_string_lossy().hash(&mut hasher);
                    hasher.finish()
                };

                Some(NativeFileSystemEntry {
                    id,
                    name: entry.file_name().to_string_lossy().to_string(),
                    path: child_path.to_string_lossy().to_string(),
                    is_dir: false,
                    size_bytes: size,
                    allocated_size_bytes: allocated,
                })
            }
        })
        .filter_map(|x| x)
        .collect();

    children.sort_unstable_by(|a, b| b.size_bytes.cmp(&a.size_bytes));

    let total_size: u64 = children.iter().map(|c| c.size_bytes).sum();
    let total_allocated: u64 = children.iter().map(|c| c.allocated_size_bytes).sum();

    state.dir_cache.insert(path_str.clone(), children);

    let id = {
        let mut hasher = DefaultHasher::new();
        path_str.hash(&mut hasher);
        hasher.finish()
    };

    NativeFileSystemEntry {
        id,
        name,
        path: path_str,
        is_dir: true,
        size_bytes: total_size,
        allocated_size_bytes: total_allocated,
    }
}