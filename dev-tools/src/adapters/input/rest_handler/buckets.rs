use crate::adapters::input::rest_handler::auth_guard::{require_any, AuthenticatedUser};
use crate::adapters::input::rest_handler::contracts::file_contracts::bucket::BucketResponseContract;
use crate::application::ports::input::files_port::DynFilesPort;
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
        &[
            uuid::uuid!("a48d7b18-ceb7-435b-b8ff-b28531f1a09f"),
            uuid::uuid!("51f20832-79a3-4c05-b4da-ca175cba2ffc"),
        ],
    )?;

    let buckets = uc.fetch_buckets().await?;
    Ok(Json(
        buckets
            .into_iter()
            .map(BucketResponseContract::from)
            .collect(),
    ))
}
