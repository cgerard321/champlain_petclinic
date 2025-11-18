use crate::adapters::input::http::guards::auth_guard::AuthenticatedUser;
use crate::adapters::input::http::rest::contracts::file_contracts::file::FileResponseContract;
use crate::application::ports::input::files_port::DynFilesPort;
use crate::shared::config;
use crate::shared::error::{AppError, AppResult};
use rocket::data::ToByteUnit;
use rocket::http::Status;
use rocket::response::status::Custom;
use rocket::serde::json::Json;
use rocket::{Data, State};
use std::path::PathBuf;

#[get("/buckets/<bucket>/files")]
pub async fn read_files(
    bucket: &str,
    port: &State<DynFilesPort>,
    user: AuthenticatedUser,
) -> AppResult<Json<Vec<FileResponseContract>>> {
    let auth_context = user.into();

    let file = port.list_files(bucket, auth_context).await?;
    Ok(Json(
        file.into_iter().map(FileResponseContract::from).collect(),
    ))
}

#[post("/buckets/<bucket>/files/<prefix..>", data = "<data>")]
pub async fn add_file(
    bucket: &str,
    prefix: PathBuf,
    data: Data<'_>,
    port: &State<DynFilesPort>,
    user: AuthenticatedUser,
) -> AppResult<Custom<Json<FileResponseContract>>> {
    let auth_context = user.into();

    let limit = config::MAX_FILE_SIZE_MB.mebibytes();
    let bytes = data
        .open(limit)
        .into_bytes()
        .await
        .map_err(|e| AppError::BadRequest(format!("read body: {e}")))?
        .into_inner();

    let file_info = port.upload(bucket, prefix, bytes, auth_context).await?;

    Ok(Custom(
        Status::Created,
        Json(FileResponseContract::from(file_info)),
    ))
}
