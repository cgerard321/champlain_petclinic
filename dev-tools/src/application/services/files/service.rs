use crate::application::ports::input::files_port::FilesPort;
use crate::application::ports::output::file_storage_port::DynFileStorage;
use crate::application::services::files::{fetch_bucket_files, fetch_buckets, upload_file};
use crate::application::services::user_context::{require_any, UserContext};
use crate::domain::entities::bucket::BucketEntity;
use crate::domain::entities::file::FileEntity;
use crate::shared::config::{ADMIN_ROLE_UUID, EDITOR_ROLE_UUID, READER_ROLE_UUID};
use crate::shared::error::AppResult;
use std::path::PathBuf;

pub struct FilesService {
    storage: DynFileStorage,
}
impl FilesService {
    pub fn new(storage: DynFileStorage) -> Self {
        Self { storage }
    }
}

#[async_trait::async_trait]
impl FilesPort for FilesService {
    async fn fetch_buckets(&self, user_ctx: UserContext) -> AppResult<Vec<BucketEntity>> {
        require_any(&user_ctx, &[ADMIN_ROLE_UUID, READER_ROLE_UUID])?;

        fetch_buckets::fetch_buckets(&self.storage).await
    }
    async fn list_files(
        &self,
        bucket: &str,
        user_ctx: UserContext,
    ) -> AppResult<Vec<FileEntity>> {
        require_any(&user_ctx, &[ADMIN_ROLE_UUID, READER_ROLE_UUID])?;

        fetch_bucket_files::fetch_files(bucket, &self.storage).await
    }
    async fn upload(
        &self,
        bucket: &str,
        prefix: PathBuf,
        bytes: Vec<u8>,
        user_ctx: UserContext,
    ) -> AppResult<FileEntity> {
        require_any(&user_ctx, &[ADMIN_ROLE_UUID, EDITOR_ROLE_UUID])?;

        upload_file::upload_file(&self.storage, bucket, prefix, bytes).await
    }
}
