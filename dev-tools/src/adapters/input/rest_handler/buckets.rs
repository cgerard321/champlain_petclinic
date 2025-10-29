use crate::adapters::input::rest_handler::auth_guard::{require_any, AuthenticatedUser};
use crate::adapters::input::rest_handler::contracts::file_contracts::bucket::BucketResponseContract;
use crate::application::ports::input::files_port::DynFilesPort;
use crate::core::config::{ADMIN_ROLE_UUID, READER_ROLE_UUID};
use crate::core::error::AppResult;
use rocket::serde::json::Json;
use rocket::State;

#[get("/buckets")]
pub async fn read_buckets(
    uc: &State<DynFilesPort>,
    user: AuthenticatedUser,
) -> AppResult<Json<Vec<BucketResponseContract>>> {
    require_any(
        &user,
        &[uuid::uuid!(ADMIN_ROLE_UUID), uuid::uuid!(READER_ROLE_UUID)],
    )?;

    let buckets = uc.fetch_buckets().await?;
    Ok(Json(
        buckets
            .into_iter()
            .map(BucketResponseContract::from)
            .collect(),
    ))
}
