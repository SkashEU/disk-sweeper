mod models;
mod scanner;

use crate::models::{ScanUpdate, ScanResult, ScanState};
use std::path::Path;
use std::sync::{atomic::Ordering, Arc};
use std::time::Duration;
use std::{fs, thread};

#[uniffi::export(callback_interface)]
pub trait ScanListener: Send + Sync {
    fn on_event(&self, event: ScanEvent);
}

#[derive(Debug, thiserror::Error, uniffi::Error)]
pub enum FileSystemError {
    #[error("File system operation failed: {val}")]
    Generic { val: String },
}

#[derive(uniffi::Enum)]
pub enum ScanEvent {
    Update {
        update: ScanUpdate
    },
    Finished {
        result: ScanResult
    },
}

#[uniffi::export]
pub fn scan_directory(input_path: String, listener: Box<dyn ScanListener>) {
    let state = Arc::new(ScanState::new());

    let state_clone = state.clone();
    let path_clone = input_path.clone();

    let scanner_handle = thread::spawn(move || {
        let results = scanner::scan_root(&path_clone, &state_clone);
        state_clone.is_complete.store(true, Ordering::SeqCst);
        results
    });

    run_reporter_loop(&listener, &state);

    let final_results = scanner_handle.join().unwrap();

    listener.on_event(ScanEvent::Finished {
        result: final_results
    });
}

#[uniffi::export]
pub fn delete_path(input_path: String, permanently: bool) -> Result<(), FileSystemError> {
    let path = Path::new(&input_path);

    let result = if permanently {
        if path.is_dir() {
            fs::remove_dir_all(path)
        } else {
            fs::remove_file(path)
        }
    } else {
        trash::delete(path).map_err(|e| std::io::Error::new(std::io::ErrorKind::Other, e.to_string()))
    };

    match result {
        Ok(_) => Ok(()),
        Err(e) => Err(FileSystemError::Generic { val: e.to_string() }),
    }
}

fn run_reporter_loop(listener: &Box<dyn ScanListener>, state: &Arc<ScanState>) {
    let update_interval = Duration::from_millis(50);

    let mut last_bytes = 0u64;
    let mut last_time = std::time::Instant::now();
    let mut avg_speed = 0.0;

    while !state.is_complete.load(Ordering::Relaxed) {
        thread::sleep(update_interval);

        let scanned_count = state.scanned_count.load(Ordering::Relaxed);
        let scanned_bytes = state.scanned_bytes.load(Ordering::Relaxed);
        let target_bytes = state.target_bytes.load(Ordering::Relaxed);
        let total_bytes = state.drive_capacity.load(Ordering::Relaxed);

        let now = std::time::Instant::now();
        let time_delta = now.duration_since(last_time).as_secs_f64();
        let bytes_delta = scanned_bytes.saturating_sub(last_bytes);

        let current_speed = if time_delta > 0.0 {
            bytes_delta as f64 / time_delta
        } else {
            0.0
        };

        if avg_speed == 0.0 {
            avg_speed = current_speed;
        } else {
            avg_speed = 0.95 * avg_speed + 0.05 * current_speed;
        }

        last_bytes = scanned_bytes;
        last_time = now;

        let remaining = target_bytes.saturating_sub(scanned_bytes);
        let eta_seconds = if avg_speed > 1024.0 {
            (remaining as f64 / avg_speed) as i64
        } else {
            0
        };

        let path_display = state.current_path.lock().map(|g| g.clone()).unwrap_or_default();

        listener.on_event(ScanEvent::Update {
            update: ScanUpdate {
                path: path_display,
                scanned_count,
                scanned_bytes,
                total_bytes,
                target_bytes,
                avg_speed: avg_speed as u64,
                eta_seconds,
            },
        });
    }
}

uniffi::setup_scaffolding!();
