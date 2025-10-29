use crate::application::services::auth::projections::AuthProjection;
use crate::core::error::AppResult;
use crate::domain::entities::user::{RoleEntity, UserEntity};
use std::collections::HashSet;
use uuid::Uuid;

#[async_trait]
pub trait UsersRepoPort: Send + Sync {
    async fn insert_user_hashed(
        &self,
        id: Uuid,
        email: &str,
        pass_hash: &[u8],
        display_name: &str,
        user_roles: HashSet<Uuid>,
    ) -> AppResult<UserEntity>;

    async fn get_user_by_id(&self, id: Uuid) -> AppResult<UserEntity>;

    async fn get_user_auth_by_email_for_login(&self, email: &str) -> AppResult<AuthProjection>;
    async fn get_roles_for_user(&self, user_id: Uuid) -> AppResult<HashSet<RoleEntity>>;
}

pub type DynUsersRepo = std::sync::Arc<dyn UsersRepoPort>;
