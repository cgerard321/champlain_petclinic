use crate::application::ports::input::files_port::DynFilesPort;
use crate::core::config;
use crate::core::error::{AppError, AppResult};
use crate::domain::models::file::FileInfo;
use crate::domain::models::user::AuthenticatedUser;
use rocket::data::ToByteUnit;
use rocket::http::Status;
use rocket::response::status::Custom;
use rocket::serde::json::Json;
use rocket::{Data, State};
use std::path::PathBuf;

#[get("/buckets/<bucket>/files")]
pub(crate) async fn read_files(
    bucket: &str,
    uc: &State<DynFilesPort>,
    _user: AuthenticatedUser,
) -> AppResult<Json<Vec<FileInfo>>> {
    Ok(Json(uc.list_files(bucket).await?))
}

#[post("/buckets/<bucket>/files/<prefix..>", data = "<data>")]
pub(crate) async fn add_file(
    bucket: &str,
    prefix: PathBuf,
    data: Data<'_>,
    uc: &State<DynFilesPort>,
    _user: AuthenticatedUser,
) -> AppResult<Custom<Json<FileInfo>>> {
    let limit = config::MAX_FILE_SIZE_MB.mebibytes();
    let bytes = data
        .open(limit)
        .into_bytes()
        .await
        .map_err(|e| AppError::BadRequest(format!("read body: {e}")))?
        .into_inner();

    let file_info = uc.upload(bucket, prefix, bytes).await?;

    Ok(Custom(Status::Created, Json(file_info)))
}
