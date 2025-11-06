use crate::application::services::user_context::UserContext;
use crate::application::services::users::params::UserCreationParams;
use crate::domain::entities::user::UserEntity;
use crate::shared::error::AppResult;

#[async_trait::async_trait]
pub trait UsersPort: Send + Sync {
    async fn create_user(
        &self,
        new_user: UserCreationParams,
        auth_context: UserContext,
    ) -> AppResult<UserEntity>;
}
pub type DynUsersPort = std::sync::Arc<dyn UsersPort>;
