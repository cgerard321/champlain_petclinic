use crate::application::ports::output::file_storage_port::DynFileStorage;
use crate::domain::entities::file::FileEntity;
use crate::shared::error::AppResult;

pub async fn fetch_files(bucket: &str, store: &DynFileStorage) -> AppResult<Vec<FileEntity>> {
    let files = store.list_bucket_files(bucket).await?;
    Ok(files)
}
