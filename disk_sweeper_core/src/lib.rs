mod models;
mod scanner;

use crate::models::{ScanUpdate, ScanState, ScanEvent};
use std::path::Path;
use std::sync::{atomic::Ordering, Arc};
use std::time::{Duration, Instant};
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

#[uniffi::export]
pub fn scan_directory(input_path: String, listener: Box<dyn ScanListener>) {
    let state = Arc::new(ScanState::new());

    let state_clone = state.clone();
    let path_clone = input_path.clone();

    let scanner_handle = thread::spawn(move || {
        let _guard = CompletionGuard {
            state: state_clone.clone(),
        };
        scanner::scan_root(&path_clone, &state_clone)
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
    let tick_rate = Duration::from_millis(50); 
    let math_rate = Duration::from_millis(1000);

    let mut last_tick = Instant::now();
    let mut last_math_calc = Instant::now();

    let mut last_bytes_at_calc = 0u64;
    let mut last_time_at_calc = Instant::now();

    let mut cached_speed: u64 = 0;
    let mut cached_eta: i64 = 0;
    let mut cached_path = String::new();

    while !state.is_complete.load(Ordering::Relaxed) {
        let elapsed = last_tick.elapsed();
        if elapsed < tick_rate {
            thread::sleep(tick_rate - elapsed);
        }
        last_tick = Instant::now();

        let scanned_count = state.scanned_count.load(Ordering::Relaxed);
        let scanned_bytes = state.scanned_bytes.load(Ordering::Relaxed);
        let target_bytes = state.target_bytes.load(Ordering::Relaxed);
        let total_bytes = state.drive_capacity.load(Ordering::Relaxed);

        if let Ok(guard) = state.current_path.try_lock() {
            cached_path = guard.clone();
        }

        if last_math_calc.elapsed() >= math_rate {
            let now = Instant::now();
            let time_delta = now.duration_since(last_time_at_calc).as_secs_f64();
            let bytes_delta = scanned_bytes.saturating_sub(last_bytes_at_calc);

            if time_delta > 0.0 {
                let current_speed = bytes_delta as f64 / time_delta;
                
                if cached_speed == 0 {
                    cached_speed = current_speed as u64;
                } else {
                    cached_speed = ((cached_speed as f64 * 0.5) + (current_speed * 0.5)) as u64;
                }

                let remaining = target_bytes.saturating_sub(scanned_bytes);
                if cached_speed > 1024 {
                    cached_eta = (remaining as f64 / cached_speed as f64) as i64;
                } else {
                    cached_eta = 0;
                }
            }
            last_bytes_at_calc = scanned_bytes;
            last_time_at_calc = now;
            last_math_calc = now;
        }
        
        listener.on_event(ScanEvent::Update {
            update: ScanUpdate {
                path: cached_path.clone(),
                scanned_count,
                scanned_bytes,
                total_bytes,
                target_bytes,
                avg_speed: cached_speed,
                eta_seconds: cached_eta,
            },
        });
    }
}

struct CompletionGuard {
    state: Arc<ScanState>,
}

impl Drop for CompletionGuard {
    fn drop(&mut self) {
        self.state.is_complete.store(true, Ordering::SeqCst);
    }
}

uniffi::setup_scaffolding!();
