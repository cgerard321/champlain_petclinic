use crate::application::ports::output::file_storage_port::FileStoragePort;
use crate::core::error::{AppError, AppResult};
use crate::domain::models::bucket::BucketInfo;
use crate::domain::models::file::FileInfo;
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
        let endpoint = env::var("MINIO_URL").map_err(|_| AppError::Internal)?;
        let access_key = env::var("FILE_ACCESS_KEY_ID").map_err(|_| AppError::Internal)?;
        let secret_key = env::var("FILE_SECRET_ACCESS_KEY").map_err(|_| AppError::Internal)?;

        let base_url: BaseUrl = endpoint.parse().map_err(|_e| AppError::Internal)?;
        let static_provider = StaticProvider::new(&access_key, &secret_key, None);

        let client = Client::new(base_url, Some(Box::new(static_provider)), None, None)
            .map_err(|_e| AppError::Internal)?;

        Ok(Self { client })
    }

    #[inline]
    pub fn client(&self) -> &Client {
        &self.client
    }
}

#[async_trait]
impl FileStoragePort for MinioStore {
    async fn list_buckets(&self) -> AppResult<Vec<BucketInfo>> {
        let resp = self.client().list_buckets().send().await?;
        let buckets = resp
            .buckets
            .into_iter()
            .map(|b| BucketInfo {
                name: b.name,
                creation_date: Some(b.creation_date.to_string()),
            })
            .collect();
        Ok(buckets)
    }

    async fn list_bucket_files(&self, bucket: &str) -> AppResult<Vec<FileInfo>> {
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
                files.push(FileInfo {
                    name: obj.name,
                    size: obj.size.unwrap_or(0) as u64,
                    last_modified: obj.last_modified.map(|dt| dt.to_string()),
                    etag: obj.etag,
                    is_latest: false,
                    version_id: None,
                });
            }
        }

        Ok(files)
    }

    async fn upload_file(
        &self,
        bucket: &str,
        extension: &str,
        prefix: PathBuf,
        bytes: Vec<u8>,
    ) -> AppResult<FileInfo> {
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

        Ok(FileInfo {
            name: resp.object,
            size: len as u64,
            last_modified: None,
            etag: Option::from(resp.etag),
            is_latest: true,
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
