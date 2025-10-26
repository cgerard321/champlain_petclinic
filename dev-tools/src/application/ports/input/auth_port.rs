use crate::core::error::AppResult;
use crate::domain::models::session::Session;
use uuid::Uuid;

#[async_trait::async_trait]
pub trait AuthPort: Send + Sync {
    async fn authenticate(&self, email: &str, password: &str) -> AppResult<Session>;
    async fn logout(&self, session_id: Uuid) -> AppResult<()>;

    async fn validate_session(&self, session_id: Uuid) -> AppResult<Uuid>;
}
pub type DynAuthPort = std::sync::Arc<dyn AuthPort>;
