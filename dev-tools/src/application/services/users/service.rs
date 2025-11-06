use crate::application::ports::input::user_port::UsersPort;
use crate::application::ports::output::user_repo_port::DynUsersRepo;
use crate::application::services::user_context::{require_any, UserContext};
use crate::application::services::users::create_user::create_user;
use crate::application::services::users::params::UserCreationParams;
use crate::domain::entities::user::UserEntity;
use crate::shared::config::SUDO_ROLE_UUID;
use crate::shared::error::AppResult;
pub struct UsersService {
    users_repo: DynUsersRepo,
}
impl UsersService {
    pub fn new(users_repo: DynUsersRepo) -> Self {
        Self { users_repo }
    }
}

#[async_trait::async_trait]
impl UsersPort for UsersService {
    async fn create_user(
        &self,
        new_user: UserCreationParams,
        auth_context: UserContext,
    ) -> AppResult<UserEntity> {
        require_any(&auth_context, &[SUDO_ROLE_UUID])?;

        create_user(&self.users_repo, new_user).await
    }
}
