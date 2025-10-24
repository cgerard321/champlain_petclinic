use crate::file_service_subdomain::data_layer::bucket_info::BucketInfo;
use crate::file_service_subdomain::data_layer::file_info::FileInfo;
use crate::handlers::global_exception_handler::{ApiError, ApiResult};
use crate::file_service_subdomain::domain_client_layer::minio_client_service::{get_buckets, get_files, post_file};

use crate::rocket::futures::StreamExt;
use std::path::PathBuf;

pub async fn fetch_buckets() -> ApiResult<Vec<BucketInfo>> {
    get_buckets().await
}

pub async fn fetch_files(bucket: &str) -> ApiResult<Vec<FileInfo>> {
    let mut stream = get_files(bucket).await?; 

    let mut files = Vec::new();

    while let Some(result) = stream.next().await {
        let page = result.map_err(ApiError::from)?;

        for item in page.contents {
            if item.is_prefix || item.is_delete_marker {
                continue;
            }

            files.push(FileInfo {
                name: item.name,
                size: item.size.unwrap_or(0),
                last_modified: item.last_modified.map(|d| d.to_rfc3339()),
                etag: item.etag.map(|e| e.trim_matches('"').to_string()),
                is_latest: item.is_latest,
                version_id: item
                    .version_id
                    .and_then(|v| if v == "null" { None } else { Some(v) }),
            });
        }
    }

    Ok(files)
}

pub async fn upload_file(
    bucket: &str,
    extension: &str,
    prefix: PathBuf,
    bytes: Vec<u8>,
) -> ApiResult<FileInfo> {
    let file_len = bytes.len();

    if file_len == 0 {
        return Err(ApiError::BadRequest("Empty file".into()));
    }

    let resp = post_file(bucket, extension, prefix, bytes).await?;

    Ok(FileInfo {
        name: resp.object,
        size: file_len as u64,
        last_modified: None,
        etag: Option::from(resp.etag),
        is_latest: true,
        version_id: resp.version_id,
    })
}
