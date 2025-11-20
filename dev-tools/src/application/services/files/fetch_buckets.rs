use crate::application::ports::output::file_storage_port::DynFileStorage;
use crate::domain::entities::bucket::BucketEntity;
use crate::shared::error::AppResult;

pub async fn fetch_buckets(storage: &DynFileStorage) -> AppResult<Vec<BucketEntity>> {
    storage.list_buckets().await
}
