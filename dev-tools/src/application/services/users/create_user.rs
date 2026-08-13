use crate::application::ports::output::crypto_port::DynCrypto;
use crate::application::ports::output::user_repo_port::DynUsersRepo;
use crate::application::services::users::params::UserCreationParams;
use crate::domain::entities::user::UserEntity;
use crate::shared::error::AppResult;
use uuid::Uuid;

pub async fn create_user(
    crypto: &DynCrypto,
    db: &DynUsersRepo,
    new_user: UserCreationParams,
) -> AppResult<UserEntity> {
    let id = Uuid::new_v4();
    let pass_hash = crypto.hash(&new_user.password)?;
    let roles = new_user.roles;

    let created = db
        .insert_user_hashed(
            id,
            &new_user.email,
            pass_hash.as_bytes(),
            &new_user.display_name,
            roles,
        )
        .await?;

    Ok(created)
}
