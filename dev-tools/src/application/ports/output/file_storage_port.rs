use crate::core::error::AppResult;
use crate::domain::models::bucket::BucketInfo;
use crate::domain::models::file::FileInfo;
use std::path::PathBuf;

#[async_trait::async_trait]
pub trait FileStoragePort: Send + Sync {
    async fn list_buckets(&self) -> AppResult<Vec<BucketInfo>>;
    async fn list_bucket_files(&self, bucket: &str) -> AppResult<Vec<FileInfo>>;
    async fn upload_file(
        &self,
        bucket: &str,
        extension: &str,
        prefix: PathBuf,
        bytes: Vec<u8>,
    ) -> AppResult<FileInfo>;
}

pub type DynFileStorage = std::sync::Arc<dyn FileStoragePort>;
