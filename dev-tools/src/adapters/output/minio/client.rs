use crate::application::ports::output::file_storage_port::FileStoragePort;
use crate::core::error::{AppError, AppResult};
use crate::domain::entities::bucket::BucketEntity;
use crate::domain::entities::file::FileEntity;
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
        let buckets = resp
            .buckets
            .into_iter()
            .map(|b| BucketEntity {
                name: b.name,
                creation_date: Some(b.creation_date.to_string()),
            })
            .collect();
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

        let mut files = Vec::new();

        while let Some(objects) = stream.next().await {
            let tmp_files = objects.map_err(AppError::from)?;

            for obj in tmp_files.contents {
                files.push(FileEntity {
                    name: obj.name,
                    size: obj.size.unwrap_or(0),
                    etag: obj.etag,
                    version_id: None,
                });
            }
        }

        log::info!("Bucket files received: {:?}", files);

        Ok(files)
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

        let len = bytes.len();

        let data = SegmentedBytes::from(Bytes::from(bytes));
        let resp = self.client().put_object(bucket, key, data).send().await?;

        Ok(FileEntity {
            name: resp.object,
            size: len as u64,
            etag: Option::from(resp.etag),
            version_id: resp.version_id,
        })
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
