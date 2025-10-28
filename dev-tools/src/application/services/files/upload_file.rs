use crate::application::ports::output::file_storage_port::DynFileStorage;
use crate::core::config;
use crate::core::error::{AppError, AppResult};
use crate::domain::entities::file::FileEntity;
use std::path::PathBuf;

pub async fn upload_file(
    store: &DynFileStorage,
    bucket: &str,
    prefix: PathBuf,
    bytes: Vec<u8>,
) -> AppResult<FileEntity> {
    let extension = infer::get(&bytes)
        .map(|k| k.extension())
        .unwrap_or(config::DEFAULT_FILE_TYPE);

    let file_len = bytes.len();

    if file_len == 0 {
        return Err(AppError::BadRequest("Empty file".into()));
    }

    let resp = store.upload_file(bucket, extension, prefix, bytes).await?;

    Ok(FileEntity {
        name: resp.name,
        size: file_len as u64,
        etag: resp.etag,
        version_id: resp.version_id,
    })
}
