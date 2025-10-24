use minio::s3::error::Error as MinioError;
use minio::s3::response::{ListObjectsResponse, PutObjectResponse};
use minio::s3::segmented_bytes::SegmentedBytes;
use minio::s3::types::{S3Api, ToStream};
use rocket::State;
use rocket::futures::Stream;
use rocket::http::hyper::body::Bytes;

use crate::minio_service::bucket::BucketInfo;
use crate::minio_service::store::MinioStore;
use crate::http::prelude::{AppError, AppResult};
use std::path::{Component, Path, PathBuf};
use uuid::Uuid;

pub async fn get_buckets(store: &State<MinioStore>) -> AppResult<Vec<BucketInfo>> {
    let resp = store.client().list_buckets().send().await?;
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
    store: &State<MinioStore>,
) -> AppResult<Box<dyn Stream<Item = Result<ListObjectsResponse, MinioError>> + Unpin + Send>> {
    let stream = store
        .client()
        .list_objects(bucket)
        .recursive(true)
        .use_api_v1(false)
        .include_versions(true)
        .to_stream()
        .await;

    Ok(stream)
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

pub async fn post_file(
    bucket: &str,
    extension: &str,
    prefix: PathBuf,
    bytes: Vec<u8>,
    store: &State<MinioStore>,
) -> AppResult<PutObjectResponse> {
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

    let data = SegmentedBytes::from(Bytes::from(bytes));
    let resp = store.client().put_object(bucket, key, data).send().await?;
    Ok(resp)
}
