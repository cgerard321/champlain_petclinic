use crate::adapters::output::minio::minio_client::get_files;
use crate::adapters::output::minio::store::MinioStore;
use crate::core::error::{AppError, AppResult};
use crate::domain::models::file::FileInfo;
use crate::rocket::futures::StreamExt;
use rocket::State;

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
