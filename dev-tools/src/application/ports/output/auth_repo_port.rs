use crate::domain::entities::session::SessionEntity;
use crate::shared::error::AppResult;
use chrono::NaiveDateTime;
use std::sync::Arc;
use uuid::Uuid;

#[async_trait::async_trait]
pub trait AuthRepoPort: Send + Sync {
    async fn insert_session(
        &self,
        session_id: Uuid,
        user_id: Uuid,
        expires_at: NaiveDateTime,
    ) -> AppResult<SessionEntity>;
    async fn find_session_by_id(&self, sid: Uuid) -> AppResult<SessionEntity>;
    async fn delete_session(&self, sid: Uuid) -> AppResult<()>;
    #[allow(dead_code)]
    async fn delete_expired_sessions(&self) -> AppResult<u64>;
}

pub type DynAuthRepo = Arc<dyn AuthRepoPort>;
