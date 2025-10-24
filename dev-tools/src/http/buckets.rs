use crate::core::error::AppResult;
use crate::file_service::store::MinioStore;
use rocket::State;
use rocket::serde::json::Json;

#[get("/buckets")]
pub(crate) async fn read_buckets(
    store: &State<MinioStore>,
) -> AppResult<Json<Vec<crate::file_service::bucket::BucketInfo>>> {
    Ok(Json(
        crate::file_service::service::fetch_buckets(store).await?,
    ))
}
