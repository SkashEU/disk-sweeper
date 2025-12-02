use dashmap::DashMap;
use std::sync::{Arc, Mutex};
use std::sync::atomic::{AtomicBool, AtomicU64, Ordering};
use std::time::Instant;

#[derive(uniffi::Enum)]
pub enum ScanEvent {
    Update { update: ScanUpdate },
    Finished { result: ScanResult },
}

#[derive(Debug, Clone, uniffi::Record)]
pub struct NativeFileSystemEntry {
    pub id: u64,
    pub name: String,
    pub path: String,
    pub is_dir: bool,
    pub size_bytes: u64,
    pub allocated_size_bytes: u64,
}

#[derive(Debug, Clone, uniffi::Record)]
pub struct StorageStats {
    pub drive_total_bytes: u64,
    pub drive_free_bytes: u64,
    pub drive_used_bytes: u64,
    pub scanned_file_count: u64,
    pub scanned_total_bytes: u64,
}

#[derive(Debug, Clone, uniffi::Record)]
pub struct ScanResult {
    pub stats: StorageStats,
    pub files: Vec<NativeFileSystemEntry>,
}

#[derive(Debug, Clone, uniffi::Record)]
pub struct ScanUpdate {
    pub path: String,
    pub scanned_count: u64,
    pub scanned_bytes: u64,
    pub total_bytes: u64,
    pub target_bytes: u64,
    pub avg_speed: u64,
    pub eta_seconds: i64,
}

#[derive(uniffi::Object)]
pub struct DiskScanner {
    pub(crate) state: Arc<ScanState>,
}

pub struct ScanState {
    pub scanned_bytes: AtomicU64,
    pub scanned_count: AtomicU64,

    pub target_bytes: AtomicU64,
    pub drive_capacity: AtomicU64,

    pub current_path: Mutex<String>,
    pub is_complete: AtomicBool,
    pub start_time: Instant,
    pub dir_cache: DashMap<String, Vec<NativeFileSystemEntry>>,
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
            dir_cache: DashMap::new(),
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
