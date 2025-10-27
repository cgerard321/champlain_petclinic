use crate::application::ports::output::auth_repo_port::DynAuthRepo;
use crate::core::error::{AppError, AppResult};
use uuid::Uuid;

pub async fn remove_session(db: &DynAuthRepo, cookie_id: Uuid) -> AppResult<()> {
    db.delete_session(cookie_id)
        .await
        .map_err(|_e| AppError::Internal)
}
