use crate::application::ports::output::file_storage_port::DynFileStorage;
use crate::core::error::AppResult;
use crate::domain::models::file::FileInfo;

pub async fn fetch_files(bucket: &str, store: &DynFileStorage) -> AppResult<Vec<FileInfo>> {
    let files = store.list_bucket_files(bucket).await?;
    Ok(files)
}
