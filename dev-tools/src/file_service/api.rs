use std::path::PathBuf;

use crate::file_service::bucket::BucketInfo;
use crate::file_service::file::FileInfo;
use crate::file_service::service::{fetch_buckets, fetch_files, upload_file};
use crate::file_service::store::MinioStore;
use crate::http::prelude::{AppError, AppResult};
use rocket::data::ToByteUnit;
use rocket::http::Status;
use rocket::response::status::Custom;
use rocket::serde::json::Json;
use rocket::{Data, State, get, post};
use crate::file_service::config::{DEFAULT_FILE_TYPE, MAX_FILE_SIZE_MB};

#[get("/buckets")]
pub(crate) async fn read_buckets(store: &State<MinioStore>) -> AppResult<Json<Vec<BucketInfo>>> {
    Ok(Json(fetch_buckets(store).await?))
}

#[get("/buckets/<bucket>/files")]
pub(crate) async fn read_files(
    bucket: &str,
    store: &State<MinioStore>,
) -> AppResult<Json<Vec<FileInfo>>> {
    Ok(Json(fetch_files(bucket, store).await?))
}

#[post("/buckets/<bucket>/files/<prefix..>", data = "<data>")]
pub(crate) async fn add_file(
    bucket: &str,
    prefix: PathBuf,
    data: Data<'_>,
    store: &State<MinioStore>,
) -> AppResult<Custom<Json<FileInfo>>> {
    let limit = MAX_FILE_SIZE_MB.mebibytes();
    let bytes = data
        .open(limit)
        .into_bytes()
        .await
        .map_err(|e| AppError::BadRequest(format!("read body: {e}")))?
        .into_inner();

    let extension = infer::get(&bytes).map(|k| k.extension()).unwrap_or(DEFAULT_FILE_TYPE);

    let file_info = upload_file(bucket, extension, prefix, bytes, store).await?;

    Ok(Custom(Status::Created, Json(file_info)))
}
