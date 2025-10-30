use crate::application::ports::output::file_storage_port::DynFileStorage;
use crate::core::error::AppResult;
use crate::domain::entities::bucket::BucketEntity;

pub async fn fetch_buckets(storage: &DynFileStorage) -> AppResult<Vec<BucketEntity>> {
    storage.list_buckets().await
}
