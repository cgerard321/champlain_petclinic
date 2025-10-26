use crate::application::ports::output::file_storage_port::DynFileStorage;
use crate::core::error::AppResult;
use crate::domain::models::file::FileInfo;
use crate::domain::models::user::AuthenticatedUser;
use crate::domain::usecases::files::fetch_bucket_files::fetch_files;
use crate::domain::usecases::files::upload_file::upload_file;
use rocket::http::Status;
use rocket::response::status::Custom;
use rocket::serde::json::Json;
use rocket::{Data, State};
use std::path::PathBuf;

#[get("/buckets/<bucket>/files")]
pub(crate) async fn read_files(
    bucket: &str,
    store: &State<DynFileStorage>,
    _user: AuthenticatedUser,
) -> AppResult<Json<Vec<FileInfo>>> {
    Ok(Json(fetch_files(bucket, store).await?))
}

#[post("/buckets/<bucket>/files/<prefix..>", data = "<data>")]
pub(crate) async fn add_file(
    bucket: &str,
    prefix: PathBuf,
    data: Data<'_>,
    store: &State<DynFileStorage>,
    _user: AuthenticatedUser,
) -> AppResult<Custom<Json<FileInfo>>> {
    let file_info = upload_file(bucket, prefix, data, store).await?;

    Ok(Custom(Status::Created, Json(file_info)))
}
