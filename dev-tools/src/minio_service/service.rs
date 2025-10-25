use crate::http::prelude::{AppError, AppResult};
use crate::minio_service::bucket::BucketInfo;
use crate::minio_service::file::FileInfo;
use crate::minio_service::minio_client::{get_buckets, get_files, post_file};
use crate::minio_service::store::MinioStore;
use crate::rocket::futures::StreamExt;
use rocket::data::ToByteUnit;
use rocket::{Data, State};
use std::path::PathBuf;

pub async fn fetch_buckets(store: &State<MinioStore>) -> AppResult<Vec<BucketInfo>> {
    get_buckets(store).await
}

pub async fn fetch_files(bucket: &str, store: &State<MinioStore>) -> AppResult<Vec<FileInfo>> {
    let mut stream = get_files(bucket, store).await?;

    let mut files = Vec::new();

    while let Some(result) = stream.next().await {
        let page = result.map_err(AppError::from)?;

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
    prefix: PathBuf,
    data: Data<'_>,
    store: &State<MinioStore>,
) -> AppResult<FileInfo> {
    let limit = crate::minio_service::config::MAX_FILE_SIZE_MB.mebibytes();
    let bytes = data
        .open(limit)
        .into_bytes()
        .await
        .map_err(|e| AppError::BadRequest(format!("read body: {e}")))?
        .into_inner();

    let extension = infer::get(&bytes)
        .map(|k| k.extension())
        .unwrap_or(crate::minio_service::config::DEFAULT_FILE_TYPE);

    let file_len = bytes.len();

    if file_len == 0 {
        return Err(AppError::BadRequest("Empty file".into()));
    }

    let resp = post_file(bucket, extension, prefix, bytes, store).await?;

    Ok(FileInfo {
        name: resp.object,
        size: file_len as u64,
        last_modified: None,
        etag: Option::from(resp.etag),
        is_latest: true,
        version_id: resp.version_id,
    })
}
