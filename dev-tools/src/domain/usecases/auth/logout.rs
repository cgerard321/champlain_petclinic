use crate::adapters::output::mysql::auth_repo;
use crate::bootstrap::Db;
use crate::core::error::{AppError, AppResult};
use rocket::State;
use uuid::Uuid;

pub async fn remove_session(db: &State<Db>, cookie_id: Uuid) -> AppResult<()> {
    auth_repo::delete_session(db, cookie_id)
        .await
        .map_err(|_e| AppError::Internal)
}
