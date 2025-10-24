use std::path::PathBuf;

use crate::business_layer::file_service_service::{fetch_buckets, fetch_files, upload_file};
use crate::data_layer::bucket_info::BucketInfo;
use crate::data_layer::file_info::FileInfo;
use crate::handlers::global_exception_handler::{ApiError, ApiResult};

use rocket::data::ToByteUnit;
use rocket::http::Status;
use rocket::response::status::Custom;
use rocket::serde::json::Json;
use rocket::{Data, get, post};

#[get("/buckets")]
pub(crate) async fn read_buckets() -> ApiResult<Json<Vec<BucketInfo>>> {
    Ok(Json(fetch_buckets().await?))
}

#[get("/buckets/<bucket>/files")]
pub(crate) async fn read_files(bucket: &str) -> ApiResult<Json<Vec<FileInfo>>> {
    Ok(Json(fetch_files(bucket).await?))
}

#[post("/buckets/<bucket>/files/<prefix..>", data = "<data>")]
pub(crate) async fn add_file(
    bucket: &str,
    prefix: PathBuf,
    data: Data<'_>,
) -> ApiResult<Custom<Json<FileInfo>>> {
    let limit = 50.mebibytes();
    let bytes = data
        .open(limit)
        .into_bytes()
        .await
        .map_err(|e| ApiError::BadRequest(format!("read body: {e}")))?
        .into_inner();

    let extension = infer::get(&bytes).map(|k| k.extension()).unwrap_or("jpeg");

    let file_info = upload_file(bucket, extension, prefix, bytes).await?;

    Ok(Custom(Status::Created, Json(file_info)))
}
