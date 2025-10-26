use crate::application::ports::output::user_repo_port::DynUsersRepo;
use crate::application::usecases::users::utils::hash_password;
use crate::core::error::{AppError, AppResult};
use crate::core::utils::auth::get_pepper;
use crate::domain::models::user::{NewUser, User};
use uuid::Uuid;

pub async fn create_user(db: &DynUsersRepo, new_user: NewUser) -> AppResult<User> {
    let pep = get_pepper();
    let id = Uuid::new_v4();
    let pass_hash = hash_password(&new_user.password, &pep)?;

    db.insert_user_hashed(
        id,
        &new_user.email,
        pass_hash.as_bytes(),
        &new_user.display_name,
    )
    .await
    .map_err(|_e| AppError::Conflict)?;

    Ok(User {
        id,
        email: new_user.email,
        display_name: new_user.display_name,
        is_active: true,
    })
}
