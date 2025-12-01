mod models;
mod scanner;

use std::path::Path;
use crate::models::ScanState;
use jni::JNIEnv;
use jni::objects::{JClass, JObject, JString, JValue};
use jni::sys::jstring;
use std::sync::{Arc, atomic::Ordering};
use std::{fs, thread};
use std::time::Duration;

const KOTLIN_SIG: &str = "(Ljava/lang/String;JJJJJJ)V";

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_skash_sweeper_data_interop_NativeFileSystemAnalyzer_scanDirectory<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    input_path: JString<'local>,
    listener: JObject<'local>,
) -> jstring {
    let path_str: String = env
        .get_string(&input_path)
        .map(|s| s.into())
        .unwrap_or_else(|_| ".".to_string());

    let state = Arc::new(ScanState::new());

    let state_clone = state.clone();
    let path_clone = path_str.clone();

    let scanner_handle = thread::spawn(move || {
        let results = scanner::scan_root(&path_clone, &state_clone);
        state_clone.is_complete.store(true, Ordering::SeqCst);
        results
    });

    run_reporter_loop(&mut env, &listener, &state);

    let results = scanner_handle.join().unwrap();
    let json = serde_json::to_string(&results).unwrap_or_else(|_| "[]".to_string());

    env.new_string(json)
        .expect("Failed to create Java String")
        .into_raw()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_skash_sweeper_data_interop_NativeFileSystemAnalyzer_deletePath<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    input_path: JString<'local>,
    permanently: jni::sys::jboolean,
) -> jstring {

    let path_str: String = env.get_string(&input_path)
        .map(|s| s.into())
        .unwrap_or_default();

    let path = Path::new(&path_str);
    let is_permanent = permanently != 0;

    let result = if is_permanent {
        if path.is_dir() {
            fs::remove_dir_all(path)
        } else {
            fs::remove_file(path)
        }
    } else {
        trash::delete(path).map_err(|e| std::io::Error::new(std::io::ErrorKind::Other, e.to_string()))
    };

    let error_msg = match result {
        Ok(_) => "".to_string(),
        Err(e) => e.to_string(),
    };

    env.new_string(error_msg)
        .expect("Failed to create Java String")
        .into_raw()
}

fn run_reporter_loop(env: &mut JNIEnv, listener: &JObject, state: &Arc<ScanState>) {
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
        let path_jstr = match env.new_string(&path_display) { Ok(s) => s, Err(_) => continue };

        let result = env.call_method(
            listener,
            "onProgress",
            KOTLIN_SIG,
            &[
                JValue::Object(&path_jstr),
                JValue::Long(scanned_count as i64),
                JValue::Long(scanned_bytes as i64),
                JValue::Long(total_bytes as i64),
                JValue::Long(target_bytes as i64),
                JValue::Long(avg_speed as i64),
                JValue::Long(eta_seconds),
            ],
        );

        if result.is_err() || env.exception_check().unwrap_or(false) {
            println!("{}", result.err().unwrap());
            break;
        }
    }
}
