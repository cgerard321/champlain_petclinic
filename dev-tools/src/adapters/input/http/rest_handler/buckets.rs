use crate::adapters::input::http::guards::auth_guard::AuthenticatedUser;
use crate::adapters::input::http::rest_handler::contracts::file_contracts::bucket::BucketResponseContract;
use crate::application::ports::input::files_port::DynFilesPort;
use crate::shared::error::AppResult;
use rocket::serde::json::Json;
use rocket::State;

#[get("/buckets")]
pub async fn read_buckets(
    port: &State<DynFilesPort>,
    user: AuthenticatedUser,
) -> AppResult<Json<Vec<BucketResponseContract>>> {
    let auth_context = user.into();

    let buckets = port.fetch_buckets(auth_context).await?;
    Ok(Json(
        buckets
            .into_iter()
            .map(BucketResponseContract::from)
            .collect(),
    ))
}
