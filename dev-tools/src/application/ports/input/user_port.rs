use crate::application::services::users::params::UserCreationParams;
use crate::core::error::AppResult;
use crate::domain::entities::user::UserEntity;

#[async_trait::async_trait]
pub trait UsersPort: Send + Sync {
    async fn create_user(&self, new_user: UserCreationParams) -> AppResult<UserEntity>;
}
pub type DynUsersPort = std::sync::Arc<dyn UsersPort>;
