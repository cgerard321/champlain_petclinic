use crate::application::ports::output::user_repo_port::DynUsersRepo;
use crate::core::error::{AppError, AppResult};
use crate::core::utils::auth::get_pepper;
use crate::domain::models::user::{NewUser, User};
use crate::domain::usecases::users::utils::hash_password;
use rocket::State;
use uuid::Uuid;

pub async fn create_user(db: &State<DynUsersRepo>, nu: NewUser) -> AppResult<User> {
    let pep = get_pepper();
    let id = Uuid::new_v4();
    let pass_hash = hash_password(&nu.password, &pep)?;

    db.insert_user_hashed(id, &nu.email, pass_hash.as_bytes(), &nu.display_name)
        .await
        .map_err(|_e| AppError::Conflict)?;

    Ok(User {
        id,
        email: nu.email,
        display_name: nu.display_name,
        is_active: true,
    })
}
