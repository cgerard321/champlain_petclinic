use crate::domain::entities::bucket::BucketEntity;
use crate::domain::entities::file::FileEntity;
use crate::shared::error::AppResult;
use std::path::PathBuf;

#[async_trait::async_trait]
pub trait FileStoragePort: Send + Sync {
    async fn list_buckets(&self) -> AppResult<Vec<BucketEntity>>;
    async fn list_bucket_files(&self, bucket: &str) -> AppResult<Vec<FileEntity>>;
    async fn upload_file(
        &self,
        bucket: &str,
        extension: &str,
        prefix: PathBuf,
        bytes: Vec<u8>,
    ) -> AppResult<FileEntity>;
}

pub type DynFileStorage = std::sync::Arc<dyn FileStoragePort>;
