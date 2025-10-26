use crate::application::ports::input::user_port::UsersPort;
use crate::application::ports::output::user_repo_port::DynUsersRepo;
use crate::application::usecases::users::create_user::create_user;
use crate::core::error::AppResult;
use crate::domain::models::user::{NewUser, User};

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
    async fn create_user(&self, new_user: NewUser) -> AppResult<User> {
        create_user(&self.users_repo, new_user).await
    }
}
