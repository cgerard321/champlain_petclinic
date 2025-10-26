use crate::adapters::output::minio::store::MinioStore;
use crate::application::ports::output::file_storage_port::DynFileStorage;
use crate::application::usecases::files::fetch_buckets::fetch_buckets;
use crate::core::error::AppResult;
use crate::domain::models::bucket::BucketInfo;
use crate::domain::models::user::AuthenticatedUser;
use rocket::serde::json::Json;
use rocket::State;

#[get("/buckets")]
pub async fn read_buckets(
    store: &State<DynFileStorage>,
    _user: AuthenticatedUser,
) -> AppResult<Json<Vec<BucketInfo>>> {
    Ok(Json(fetch_buckets(store).await?))
}
