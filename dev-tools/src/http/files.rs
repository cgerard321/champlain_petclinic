use crate::auth::session_guard::AuthenticatedUser;
use crate::core::error::AppResult;
use crate::minio::file::FileInfo;
use crate::minio::service;
use crate::minio::store::MinioStore;
use rocket::http::Status;
use rocket::response::status::Custom;
use rocket::serde::json::Json;
use rocket::{Data, State};
use std::path::PathBuf;

#[get("/buckets/<bucket>/files")]
pub(crate) async fn read_files(
    bucket: &str,
    store: &State<MinioStore>,
    _user: AuthenticatedUser,
) -> AppResult<Json<Vec<FileInfo>>> {
    Ok(Json(service::fetch_files(bucket, store).await?))
}

#[post("/buckets/<bucket>/files/<prefix..>", data = "<data>")]
pub(crate) async fn add_file(
    bucket: &str,
    prefix: PathBuf,
    data: Data<'_>,
    store: &State<MinioStore>,
    _user: AuthenticatedUser,
) -> AppResult<Custom<Json<FileInfo>>> {
    let file_info = service::upload_file(bucket, prefix, data, store).await?;

    Ok(Custom(Status::Created, Json(file_info)))
}
