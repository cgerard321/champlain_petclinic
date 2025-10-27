use crate::core::error::AppResult;
use crate::domain::models::user::FullUser;
use uuid::Uuid;

#[async_trait]
pub trait UsersRepoPort: Send + Sync {
    async fn insert_user_hashed(
        &self,
        id: Uuid,
        email: &str,
        pass_hash: &[u8],
        display_name: &str,
    ) -> AppResult<()>;

    async fn get_user_auth_by_email_full(&self, email: &str) -> AppResult<FullUser>;
}

pub type DynUsersRepo = std::sync::Arc<dyn UsersRepoPort>;
