use serde::Serialize;
use std::sync::Mutex;
use std::sync::atomic::{AtomicBool, AtomicU64, Ordering};
use std::time::Instant;

#[derive(Serialize)]
pub struct FileItem {
    pub name: String,
    pub path: String,
    pub is_dir: bool,
    pub size_bytes: u64,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
pub struct DiskStats {
    pub drive_total_bytes: u64,
    pub drive_free_bytes: u64,
    pub drive_used_bytes: u64,
    pub scanned_file_count: u64,
    pub scanned_total_bytes: u64,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
pub struct ScanResponse {
    pub stats: DiskStats,
    pub files: Vec<FileItem>,
}

pub struct ScanState {
    pub scanned_bytes: AtomicU64,
    pub scanned_count: AtomicU64,

    pub target_bytes: AtomicU64,
    pub drive_capacity: AtomicU64,

    pub current_path: Mutex<String>,
    pub is_complete: AtomicBool,
    pub start_time: Instant,
}

impl ScanState {
    pub fn new() -> Self {
        Self {
            scanned_bytes: AtomicU64::new(0),
            scanned_count: AtomicU64::new(0),
            target_bytes: AtomicU64::new(0),
            drive_capacity: AtomicU64::new(0),
            current_path: Mutex::new(String::from("Initializing...")),
            is_complete: AtomicBool::new(false),
            start_time: Instant::now(),
        }
    }

    pub fn add_file(&self, size: u64) {
        self.scanned_bytes.fetch_add(size, Ordering::Relaxed);
        self.scanned_count.fetch_add(1, Ordering::Relaxed);
    }

    pub fn try_update_path(&self, path: &str) {
        if let Ok(mut guard) = self.current_path.try_lock() {
            *guard = path.to_string();
        }
    }
}
