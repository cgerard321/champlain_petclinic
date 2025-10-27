use crate::core::error::AppResult;
use crate::domain::models::user::{NewUser, User};

#[async_trait::async_trait]
pub trait UsersPort: Send + Sync {
    async fn create_user(&self, new_user: NewUser) -> AppResult<User>;
}
pub type DynUsersPort = std::sync::Arc<dyn UsersPort>;
