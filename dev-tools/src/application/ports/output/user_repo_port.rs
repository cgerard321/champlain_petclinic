use crate::core::error::AppResult;
use crate::domain::entities::user::UserEntity;
use uuid::Uuid;
use crate::application::services::auth::projections::AuthProjection;

#[async_trait]
pub trait UsersRepoPort: Send + Sync {
    async fn insert_user_hashed(
        &self,
        id: Uuid,
        email: &str,
        pass_hash: &[u8],
        display_name: &str,
    ) -> AppResult<UserEntity>;
    
    async fn get_user_by_id(&self, id: Uuid) -> AppResult<UserEntity>;

    async fn get_user_auth_by_email_for_login(&self, email: &str) -> AppResult<AuthProjection>;
}

pub type DynUsersRepo = std::sync::Arc<dyn UsersRepoPort>;
