use crate::application::ports::input::files_port::DynFilesPort;
use crate::core::error::AppResult;
use crate::domain::models::bucket::BucketInfo;
use crate::domain::models::user::AuthenticatedUser;
use rocket::serde::json::Json;
use rocket::State;

#[get("/buckets")]
pub async fn read_buckets(
    uc: &State<DynFilesPort>,
    _user: AuthenticatedUser,
) -> AppResult<Json<Vec<BucketInfo>>> {
    Ok(Json(uc.fetch_buckets().await?))
}
