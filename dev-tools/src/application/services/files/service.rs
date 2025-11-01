use crate::application::ports::input::files_port::FilesPort;
use crate::application::ports::output::file_storage_port::DynFileStorage;
use crate::application::services::auth_context::AuthContext;
use crate::application::services::files::{fetch_bucket_files, fetch_buckets, upload_file};
use crate::application::services::utils::require_any;
use crate::core::config::{ADMIN_ROLE_UUID, EDITOR_ROLE_UUID, READER_ROLE_UUID};
use crate::core::error::AppResult;
use crate::domain::entities::bucket::BucketEntity;
use crate::domain::entities::file::FileEntity;
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
    async fn fetch_buckets(&self, auth_context: AuthContext) -> AppResult<Vec<BucketEntity>> {
        require_any(&auth_context, &[ADMIN_ROLE_UUID, READER_ROLE_UUID])?;

        fetch_buckets::fetch_buckets(&self.storage).await
    }
    async fn list_files(
        &self,
        bucket: &str,
        auth_context: AuthContext,
    ) -> AppResult<Vec<FileEntity>> {
        require_any(&auth_context, &[ADMIN_ROLE_UUID, READER_ROLE_UUID])?;

        fetch_bucket_files::fetch_files(bucket, &self.storage).await
    }
    async fn upload(
        &self,
        bucket: &str,
        prefix: PathBuf,
        bytes: Vec<u8>,
        auth_context: AuthContext,
    ) -> AppResult<FileEntity> {
        require_any(&auth_context, &[ADMIN_ROLE_UUID, EDITOR_ROLE_UUID])?;

        upload_file::upload_file(&self.storage, bucket, prefix, bytes).await
    }
}
