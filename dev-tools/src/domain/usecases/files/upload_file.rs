use crate::adapters::output::minio::config;
use crate::adapters::output::minio::minio_client::post_file;
use crate::adapters::output::minio::store::MinioStore;
use crate::core::error::{AppError, AppResult};
use crate::domain::models::file::FileInfo;
use rocket::data::ToByteUnit;
use rocket::{Data, State};
use std::path::PathBuf;

pub async fn upload_file(
    bucket: &str,
    prefix: PathBuf,
    data: Data<'_>,
    store: &State<MinioStore>,
) -> AppResult<FileInfo> {
    let limit = config::MAX_FILE_SIZE_MB.mebibytes();
    let bytes = data
        .open(limit)
        .into_bytes()
        .await
        .map_err(|e| AppError::BadRequest(format!("read body: {e}")))?
        .into_inner();

    let extension = infer::get(&bytes)
        .map(|k| k.extension())
        .unwrap_or(config::DEFAULT_FILE_TYPE);

    let file_len = bytes.len();

    if file_len == 0 {
        return Err(AppError::BadRequest("Empty file".into()));
    }

    let resp = post_file(bucket, extension, prefix, bytes, store).await?;

    Ok(FileInfo {
        name: resp.object,
        size: file_len as u64,
        last_modified: None,
        etag: Option::from(resp.etag),
        is_latest: true,
        version_id: resp.version_id,
    })
}
