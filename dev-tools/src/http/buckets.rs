use crate::core::error::AppResult;
use crate::minio_service::store::MinioStore;
use rocket::State;
use rocket::serde::json::Json;

#[get("/buckets")]
pub(crate) async fn read_buckets(
    store: &State<MinioStore>,
) -> AppResult<Json<Vec<crate::minio_service::bucket::BucketInfo>>> {
    Ok(Json(
        crate::minio_service::service::fetch_buckets(store).await?,
    ))
}
