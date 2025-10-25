use crate::db::database::Db;
use crate::http::prelude::{AppError, AppResult};
use crate::users::repo;
use crate::users::user::{FullUser, NewUser, User};

use crate::db::error::map_sqlx_err;

use crate::utils::auth::get_pepper;
use argon2::password_hash::{rand_core::OsRng, SaltString};
use argon2::{Argon2, PasswordHasher};
use rocket::State;
use uuid::Uuid;

fn hash_password(plain: &str, pepper: &str) -> AppResult<String> {
    let salt = SaltString::generate(&mut OsRng);
    let argon = Argon2::default();
    argon
        .hash_password(format!("{plain}{pepper}").as_bytes(), &salt)
        .map(|h| h.to_string())
        .map_err(|_| AppError::UnprocessableEntity("argon2 hash failed".into()))
}

pub async fn insert_user(db: &State<Db>, nu: NewUser) -> AppResult<User> {
    let pep = get_pepper();
    let id = Uuid::new_v4();
    let pass_hash = hash_password(&nu.password, &pep)?;

    repo::insert_user_hashed(db, id, &nu.email, pass_hash.as_bytes(), &nu.display_name)
        .await
        .map_err(|e| map_sqlx_err(e, "User"))?;

    Ok(User {
        id,
        email: nu.email,
        display_name: nu.display_name,
        is_active: true,
    })
}

pub async fn get_user_for_login(db: &State<Db>, email: &str) -> AppResult<FullUser> {
    let row = repo::get_user_auth_by_email(db, email)
        .await
        .map_err(|e| map_sqlx_err(e, "User"))?;

    Ok(row)
}
