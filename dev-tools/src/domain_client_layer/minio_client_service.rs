use crate::data_layer::bucket_info::BucketInfo;
use crate::handlers::global_exception_handler::{ApiError, ApiResult};

use minio::s3::Client;
use minio::s3::creds::StaticProvider;
use minio::s3::error::Error as MinioError;
use minio::s3::http::BaseUrl;
use minio::s3::response::{ListObjectsResponse, PutObjectResponse};
use minio::s3::segmented_bytes::SegmentedBytes;
use minio::s3::types::{S3Api, ToStream};

use rocket::futures::Stream;
use rocket::http::hyper::body::Bytes;

use std::env;
use std::path::{Component, PathBuf};
use uuid::Uuid;

fn client() -> ApiResult<Client> {
    let endpoint =
        env::var("MINIO_URL").map_err(|_| ApiError::Internal("Missing env MINIO_URL".into()))?;
    let access_key = env::var("FILE_ACCESS_KEY_ID")
        .map_err(|_| ApiError::Internal("Missing env FILE_ACCESS_KEY_ID".into()))?;
    let secret_key = env::var("FILE_SECRET_ACCESS_KEY")
        .map_err(|_| ApiError::Internal("Missing env FILE_SECRET_ACCESS_KEY".into()))?;

    let base_url: BaseUrl = endpoint
        .parse()
        .map_err(|e| ApiError::Internal(format!("Invalid MINIO_URL: {e}")))?;
    let static_provider = StaticProvider::new(&access_key, &secret_key, None);

    Client::new(base_url, Some(Box::new(static_provider)), None, None)
        .map_err(|e| ApiError::Internal(format!("MinIO client init failed: {e}")))
}

pub async fn get_buckets() -> ApiResult<Vec<BucketInfo>> {
    let c = client()?;
    let resp = c.list_buckets().send().await?;

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

pub async fn get_files(
    bucket: &str,
) -> ApiResult<Box<dyn Stream<Item = Result<ListObjectsResponse, MinioError>> + Unpin + Send>> {
    let c = client()?;

    let stream = c
        .list_objects(bucket)
        .recursive(true)
        .use_api_v1(false)
        .include_versions(true)
        .to_stream()
        .await;

    Ok(stream)
}

fn sanitize_prefix(prefix: &PathBuf) -> ApiResult<String> {
    let mut parts = Vec::<String>::new();
    for comp in prefix.components() {
        match comp {
            Component::Normal(s) => {
                let seg = s.to_string_lossy();
                if seg.is_empty() {
                    continue;
                }
                parts.push(seg.to_string());
            }
            _ => return Err(ApiError::BadRequest("Invalid folder name".into())),
        }
    }
    Ok(parts.join("/"))
}

pub async fn post_file(
    bucket: &str,
    extension: &str,
    prefix: PathBuf,
    bytes: Vec<u8>,
) -> ApiResult<PutObjectResponse> {
    let c = client()?;

    let clean_prefix = sanitize_prefix(&prefix)?;

    let ext = extension.trim().trim_matches('.');
    if ext.is_empty() {
        return Err(ApiError::BadRequest("Empty/invalid file extension".into()));
    }

    let key = if clean_prefix.is_empty() {
        format!("{}.{}", Uuid::new_v4(), ext)
    } else {
        format!("{}/{}.{}", clean_prefix, Uuid::new_v4(), ext)
    };

    let data = SegmentedBytes::from(Bytes::from(bytes));

    let resp = c.put_object(bucket, key, data).send().await?;
    Ok(resp)
}
