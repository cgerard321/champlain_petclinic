use crate::core::error::{AppError, AppResult};
use crate::minio_service::store::MinioStore;
use rocket::data::ToByteUnit;
use rocket::http::Status;
use rocket::response::status::Custom;
use rocket::serde::json::Json;
use rocket::{Data, State};
use std::path::PathBuf;

#[get("/buckets/<bucket>/files")]
pub(crate) async fn read_files(
    bucket: &str,
    store: &State<MinioStore>,
) -> AppResult<Json<Vec<crate::minio_service::file::FileInfo>>> {
    Ok(Json(
        crate::minio_service::service::fetch_files(bucket, store).await?,
    ))
}

#[post("/buckets/<bucket>/files/<prefix..>", data = "<data>")]
pub(crate) async fn add_file(
    bucket: &str,
    prefix: PathBuf,
    data: Data<'_>,
    store: &State<MinioStore>,
) -> AppResult<Custom<Json<crate::minio_service::file::FileInfo>>> {
    let file_info = crate::minio_service::service::upload_file(bucket, prefix, data, store).await?;

    Ok(Custom(Status::Created, Json(file_info)))
}
