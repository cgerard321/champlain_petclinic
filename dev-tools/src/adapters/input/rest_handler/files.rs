use crate::adapters::input::rest_handler::dtos::file::file_dto::FileDto;
use crate::adapters::input::rest_handler::session_guard::AuthenticatedUser;
use crate::application::ports::input::files_port::DynFilesPort;
use crate::core::config;
use crate::core::error::{AppError, AppResult};
use rocket::data::ToByteUnit;
use rocket::http::Status;
use rocket::response::status::Custom;
use rocket::serde::json::Json;
use rocket::{Data, State};
use std::path::PathBuf;

#[get("/buckets/<bucket>/files")]
pub async fn read_files(
    bucket: &str,
    uc: &State<DynFilesPort>,
    _user: AuthenticatedUser,
) -> AppResult<Json<Vec<FileDto>>> {
    let file = uc.list_files(bucket).await?;
    Ok(Json(
        file.into_iter().map(|file| FileDto::from(file)).collect(),
    ))
}

#[post("/buckets/<bucket>/files/<prefix..>", data = "<data>")]
pub async fn add_file(
    bucket: &str,
    prefix: PathBuf,
    data: Data<'_>,
    uc: &State<DynFilesPort>,
    _user: AuthenticatedUser,
) -> AppResult<Custom<Json<FileDto>>> {
    let limit = config::MAX_FILE_SIZE_MB.mebibytes();
    let bytes = data
        .open(limit)
        .into_bytes()
        .await
        .map_err(|e| AppError::BadRequest(format!("read body: {e}")))?
        .into_inner();

    let file_info = uc.upload(bucket, prefix, bytes).await?;

    Ok(Custom(
        Status::Created,
        Json(FileDto::from(file_info)),
    ))
}
