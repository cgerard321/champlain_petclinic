use crate::application::ports::output::user_repo_port::DynUsersRepo;
use crate::application::services::users::params::UserCreationParams;
use crate::application::services::users::utils::hash_password;
use crate::core::error::AppResult;
use crate::core::utils::auth::get_pepper;
use crate::domain::entities::user::UserEntity;
use uuid::Uuid;

pub async fn create_user(db: &DynUsersRepo, new_user: UserCreationParams) -> AppResult<UserEntity> {
    let pep = get_pepper();
    let id = Uuid::new_v4();
    let pass_hash = hash_password(&new_user.password, &pep)?;

    let created = db
        .insert_user_hashed(
            id,
            &new_user.email,
            pass_hash.as_bytes(),
            &new_user.display_name,
        )
        .await?;

    Ok(UserEntity {
        user_id: created.user_id,
        email: created.email,
        display_name: created.display_name,
        is_active: created.is_active,
    })
}
