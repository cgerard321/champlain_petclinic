use crate::core::error::AppResult;
use crate::domain::entities::bucket::BucketEntity;
use crate::domain::entities::file::FileEntity;
use std::path::PathBuf;

#[async_trait::async_trait]
pub trait FilesPort: Send + Sync {
    async fn fetch_buckets(&self) -> AppResult<Vec<BucketEntity>>;
    async fn list_files(&self, bucket: &str) -> AppResult<Vec<FileEntity>>;
    async fn upload(&self, bucket: &str, prefix: PathBuf, bytes: Vec<u8>) -> AppResult<FileEntity>;
}
pub type DynFilesPort = std::sync::Arc<dyn FilesPort>;
