use crate::adapters::input::rest_handler::contracts::file_contracts::bucket::BucketResponseContract;
use crate::adapters::input::rest_handler::session_guard::AuthenticatedUser;
use crate::application::ports::input::files_port::DynFilesPort;
use crate::core::error::AppResult;
use rocket::serde::json::Json;
use rocket::State;

#[get("/buckets")]
pub async fn read_buckets(
    uc: &State<DynFilesPort>,
    _user: AuthenticatedUser,
) -> AppResult<Json<Vec<BucketResponseContract>>> {
    let buckets = uc.fetch_buckets().await?;
    Ok(Json(buckets.into_iter().map(BucketResponseContract::from).collect()))
}
