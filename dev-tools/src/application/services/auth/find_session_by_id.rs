use crate::application::ports::output::auth_repo_port::DynAuthRepo;
use crate::domain::entities::session::SessionEntity;

pub async fn find_session_by_id(
    db: &DynAuthRepo,
    session_id: uuid::Uuid,
) -> crate::core::error::AppResult<SessionEntity> {
    db.find_session_by_id(session_id)
        .await
        .map_err(|_e| crate::core::error::AppError::Internal)
}
