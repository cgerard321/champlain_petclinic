use crate::core::error::AppResult;
use crate::domain::models::session::Session;
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
    ) -> AppResult<Session>;
    async fn find_session_by_id(&self, sid: Uuid) -> AppResult<Session>;
    async fn delete_session(&self, sid: Uuid) -> AppResult<()>;
    async fn delete_expired_sessions(&self) -> AppResult<u64>;
}

pub type DynAuthRepo = Arc<dyn AuthRepoPort>;
