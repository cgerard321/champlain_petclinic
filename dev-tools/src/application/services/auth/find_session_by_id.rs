use crate::application::ports::output::auth_repo_port::DynAuthRepo;
use crate::domain::entities::session::SessionEntity;
use crate::shared::error::AppResult;

pub async fn find_session_by_id(
    db: &DynAuthRepo,
    session_id: uuid::Uuid,
) -> AppResult<SessionEntity> {
    db.find_session_by_id(session_id).await
}
