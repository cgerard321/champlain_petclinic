use crate::adapters::output::minio::mappers::file_mapper::PutObjectMap;
use crate::application::ports::output::file_storage_port::FileStoragePort;
use crate::domain::entities::bucket::BucketEntity;
use crate::domain::entities::file::FileEntity;
use crate::shared::error::{AppError, AppResult};
use bytes::Bytes;
use futures::StreamExt;
use minio::s3::creds::StaticProvider;
use minio::s3::http::BaseUrl;
use minio::s3::segmented_bytes::SegmentedBytes;
use minio::s3::types::{S3Api, ToStream};
use minio::s3::Client;
use std::env;
use std::path::{Component, Path, PathBuf};
use uuid::Uuid;

pub struct MinioStore {
    client: Client,
}

impl MinioStore {
    pub fn from_env() -> AppResult<Self> {
        log::info!("Connecting to minio");
        let endpoint = env::var("MINIO_URL").map_err(|_| AppError::Internal)?;
        let access_key = env::var("FILE_ACCESS_KEY_ID").map_err(|_| AppError::Internal)?;
        let secret_key = env::var("FILE_SECRET_ACCESS_KEY").map_err(|_| AppError::Internal)?;

        let base_url: BaseUrl = endpoint.parse().map_err(|_e| AppError::Internal)?;
        let static_provider = StaticProvider::new(&access_key, &secret_key, None);

        let client = Client::new(base_url, Some(Box::new(static_provider)), None, None)
            .map_err(|_e| AppError::Internal)?;

        log::info!("Connected to minio");

        Ok(Self { client })
    }

    #[inline]
    pub fn client(&self) -> &Client {
        &self.client
    }
}

#[async_trait]
impl FileStoragePort for MinioStore {
    async fn list_buckets(&self) -> AppResult<Vec<BucketEntity>> {
        log::info!("Getting buckets");
        let resp = self.client().list_buckets().send().await?;
        let buckets: Vec<BucketEntity> = resp.buckets.into_iter().map(Into::into).collect();

        log::info!("Buckets received: {:?}", buckets);
        Ok(buckets)
    }

    async fn list_bucket_files(&self, bucket: &str) -> AppResult<Vec<FileEntity>> {
        log::info!("Getting bucket files");
        let mut stream = self
            .client()
            .list_objects(bucket)
            .recursive(true)
            .use_api_v1(false)
            .include_versions(true)
            .to_stream()
            .await;

        let mut all = Vec::<FileEntity>::new();

        while let Some(page) = stream.next().await {
            let page = page.map_err(AppError::from)?;
            all.extend(page.contents.into_iter().map(Into::into));
        }

        log::info!("Bucket files received: {:?}", all);

        Ok(all)
    }

    async fn upload_file(
        &self,
        bucket: &str,
        extension: &str,
        prefix: PathBuf,
        bytes: Vec<u8>,
    ) -> AppResult<FileEntity> {
        log::info!("Uploading file");
        let clean_prefix = sanitize_prefix(&prefix)?;

        let ext = extension.trim().trim_matches('.');
        if ext.is_empty() {
            return Err(AppError::BadRequest("Empty/invalid file extension".into()));
        }

        let key = if clean_prefix.is_empty() {
            format!("{}.{}", Uuid::new_v4(), ext)
        } else {
            format!("{}/{}.{}", clean_prefix, Uuid::new_v4(), ext)
        };

        let size = bytes.len() as u64;

        let data = SegmentedBytes::from(Bytes::from(bytes));
        let resp = self.client().put_object(bucket, key, data).send().await?;
        let file: FileEntity = PutObjectMap { resp, size }.into();

        Ok(file)
    }
}

fn sanitize_prefix(prefix: &Path) -> AppResult<String> {
    let mut parts = Vec::<String>::new();
    for comp in prefix.components() {
        match comp {
            Component::Normal(s) => {
                let seg = s.to_string_lossy();
                if !seg.is_empty() {
                    parts.push(seg.to_string());
                }
            }
            _ => return Err(AppError::BadRequest("Invalid folder name".into())),
        }
    }
    Ok(parts.join("/"))
}
