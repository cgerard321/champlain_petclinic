use crate::core::error::AppResult;
use crate::minio::store::MinioStore;
use rocket::serde::json::Json;
use rocket::State;
use crate::users::user::AuthenticatedUser;

#[get("/buckets")]
pub(crate) async fn read_buckets(
    store: &State<MinioStore>,
    _user: AuthenticatedUser
) -> AppResult<Json<Vec<crate::minio::bucket::BucketInfo>>> {
    Ok(Json(crate::minio::service::fetch_buckets(store).await?))
}
