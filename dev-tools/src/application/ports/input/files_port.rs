use crate::application::services::auth_context::AuthContext;
use crate::core::error::AppResult;
use crate::domain::entities::bucket::BucketEntity;
use crate::domain::entities::file::FileEntity;
use std::path::PathBuf;

#[async_trait::async_trait]
pub trait FilesPort: Send + Sync {
    async fn fetch_buckets(&self, auth_context: AuthContext) -> AppResult<Vec<BucketEntity>>;
    async fn list_files(
        &self,
        bucket: &str,
        auth_context: AuthContext,
    ) -> AppResult<Vec<FileEntity>>;
    async fn upload(
        &self,
        bucket: &str,
        prefix: PathBuf,
        bytes: Vec<u8>,
        auth_context: AuthContext,
    ) -> AppResult<FileEntity>;
}
pub type DynFilesPort = std::sync::Arc<dyn FilesPort>;
