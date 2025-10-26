use crate::adapters::output::minio::store::MinioStore;
use crate::core::error::AppResult;
use crate::domain::models::bucket::BucketInfo;
use crate::domain::models::user::AuthenticatedUser;
use crate::domain::usecases::files::fetch_buckets::fetch_buckets;
use rocket::serde::json::Json;
use rocket::State;

#[get("/buckets")]
pub(crate) async fn read_buckets(
    store: &State<MinioStore>,
    _user: AuthenticatedUser,
) -> AppResult<Json<Vec<BucketInfo>>> {
    Ok(Json(fetch_buckets(store).await?))
}
