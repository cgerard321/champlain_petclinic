use crate::application::services::auth::params::UserLoginParams;
use crate::domain::entities::session::SessionEntity;
use crate::domain::entities::user::UserEntity;
use crate::shared::error::AppResult;
use uuid::Uuid;

#[async_trait::async_trait]
pub trait AuthPort: Send + Sync {
    async fn authenticate(&self, login_info: UserLoginParams) -> AppResult<SessionEntity>;
    async fn logout(&self, session_id: Uuid) -> AppResult<()>;
    async fn validate_session(&self, session_id: Uuid) -> AppResult<UserEntity>;
}
pub type DynAuthPort = std::sync::Arc<dyn AuthPort>;
