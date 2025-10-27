use crate::core::error::AppResult;
use crate::domain::models::{bucket::BucketInfo, file::FileInfo};
use std::path::PathBuf;

#[async_trait::async_trait]
pub trait FilesPort: Send + Sync {
    async fn fetch_buckets(&self) -> AppResult<Vec<BucketInfo>>;
    async fn list_files(&self, bucket: &str) -> AppResult<Vec<FileInfo>>;
    async fn upload(&self, bucket: &str, prefix: PathBuf, bytes: Vec<u8>) -> AppResult<FileInfo>;
}
pub type DynFilesPort = std::sync::Arc<dyn FilesPort>;
