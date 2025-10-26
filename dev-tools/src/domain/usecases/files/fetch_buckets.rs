use crate::adapters::output::minio::minio_client::get_buckets;
use crate::adapters::output::minio::store::MinioStore;
use crate::core::error::AppResult;
use crate::domain::models::bucket::BucketInfo;
use rocket::State;

pub async fn fetch_buckets(store: &State<MinioStore>) -> AppResult<Vec<BucketInfo>> {
    get_buckets(store).await
}
