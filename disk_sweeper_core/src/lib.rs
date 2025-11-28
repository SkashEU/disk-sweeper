use std::path::Path;
use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;
use jwalk::WalkDir;
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize)]
struct FileItem {
    name: String,
    path: String,
    is_dir: bool,
    size_bytes: u64,
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_skash_sweeper_data_interop_NativeFileSystemAnalyzer_scanDirectory<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    input_path: JString<'local>
) -> jstring {
    let path: String = match env.get_string(&input_path) {
        Ok(value) => value.into(),
        Err(_) => return env.new_string("[]").unwrap().into_raw()
    };

    let items = scan_folder(&path);

    let json_output = serde_json::to_string(&items).unwrap_or("[]".to_string());

    env.new_string(json_output)
        .expect("Failed to create JSON string")
        .into_raw()
}

fn scan_folder(path_str: &str) -> Vec<FileItem> {
    let path = Path::new(path_str);
    let mut results = Vec::new();

    if let Ok(entries) = std::fs::read_dir(path) {
        for entry in entries.flatten() {
            let meta = entry.metadata();

            if meta.is_err() { continue; }
            let meta = meta.unwrap();

            let file_name = entry.file_name().to_string_lossy().to_string();
            let full_path = entry.path().to_string_lossy().to_string();
            let is_dir = meta.is_dir();

            let size = if is_dir { calculate_dir_size(&full_path) } else { meta.len() };

            results.push(FileItem {
                name: file_name,
                path: full_path,
                is_dir: meta.is_dir(),
                size_bytes: size,
            });
        }
    }

    results.sort_by(|a, b| b.size_bytes.cmp(&a.size_bytes));
    results
}

fn calculate_dir_size(path_str: &str) -> u64 {
    WalkDir::new(path_str)
        .skip_hidden(false)
        .into_iter()
        .map(|entry| {
            match entry {
                Ok(entry) => {
                    if let Ok(metadata) = entry.metadata() {
                        if metadata.is_file() { metadata.len() } else { 0 }
                    } else { 0 }
                }
                Err(_) => 0,
            }
        }).sum()
}