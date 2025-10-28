use crate::application::ports::input::files_port::FilesPort;
use crate::application::ports::output::file_storage_port::DynFileStorage;
use crate::application::services::files::{fetch_bucket_files, fetch_buckets, upload_file};
use crate::core::error::AppResult;
use crate::domain::entities::bucket::BucketEntity;
use std::path::PathBuf;
use crate::domain::entities::file::FileEntity;

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
    async fn fetch_buckets(&self) -> AppResult<Vec<BucketEntity>> {
        fetch_buckets::fetch_buckets(&self.storage).await
    }
    async fn list_files(&self, bucket: &str) -> AppResult<Vec<FileEntity>> {
        fetch_bucket_files::fetch_files(bucket, &self.storage).await
    }
    async fn upload(&self, bucket: &str, prefix: PathBuf, bytes: Vec<u8>) -> AppResult<FileEntity> {
        upload_file::upload_file(&self.storage, bucket, prefix, bytes).await
    }
}
