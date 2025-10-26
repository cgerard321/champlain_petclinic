use crate::application::ports::output::file_storage_port::DynFileStorage;
use crate::core::error::AppResult;
use crate::domain::models::bucket::BucketInfo;

pub async fn fetch_buckets(storage: &DynFileStorage) -> AppResult<Vec<BucketInfo>> {
    storage.list_buckets().await
}
