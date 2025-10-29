use crate::application::services::auth::params::UserLoginParams;
use crate::core::error::AppResult;
use crate::domain::entities::session::SessionEntity;
use uuid::Uuid;

#[async_trait::async_trait]
pub trait AuthPort: Send + Sync {
    async fn authenticate(&self, login_info: UserLoginParams) -> AppResult<SessionEntity>;
    async fn logout(&self, session_id: Uuid) -> AppResult<()>;

    async fn validate_session(&self, session_id: Uuid) -> AppResult<Uuid>;
}
pub type DynAuthPort = std::sync::Arc<dyn AuthPort>;
