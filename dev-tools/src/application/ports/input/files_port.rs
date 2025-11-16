use crate::application::services::user_context::UserContext;
use crate::domain::entities::bucket::BucketEntity;
use crate::domain::entities::file::FileEntity;
use crate::shared::error::AppResult;
use std::path::PathBuf;

#[async_trait::async_trait]
pub trait FilesPort: Send + Sync {
    async fn fetch_buckets(&self, auth_context: UserContext) -> AppResult<Vec<BucketEntity>>;
    async fn list_files(&self, bucket: &str, user_ctx: UserContext) -> AppResult<Vec<FileEntity>>;
    async fn upload(
        &self,
        bucket: &str,
        prefix: PathBuf,
        bytes: Vec<u8>,
        user_ctx: UserContext,
    ) -> AppResult<FileEntity>;
}
pub type DynFilesPort = std::sync::Arc<dyn FilesPort>;
