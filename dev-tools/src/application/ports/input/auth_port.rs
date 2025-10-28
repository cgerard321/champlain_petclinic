use crate::core::error::AppResult;
use uuid::Uuid;
use crate::domain::entities::session::SessionEntity;

#[async_trait::async_trait]
pub trait AuthPort: Send + Sync {
    async fn authenticate(&self, email: &str, password: &str) -> AppResult<SessionEntity>;
    async fn logout(&self, session_id: Uuid) -> AppResult<()>;

    async fn validate_session(&self, session_id: Uuid) -> AppResult<Uuid>;
}
pub type DynAuthPort = std::sync::Arc<dyn AuthPort>;
