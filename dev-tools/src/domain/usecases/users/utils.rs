use crate::core::error::{AppError, AppResult};
use argon2::password_hash::rand_core::OsRng;
use argon2::password_hash::SaltString;
use argon2::{Argon2, PasswordHasher};

pub fn hash_password(plain: &str, pepper: &str) -> AppResult<String> {
    let salt = SaltString::generate(&mut OsRng);
    let argon = Argon2::default();
    argon
        .hash_password(format!("{plain}{pepper}").as_bytes(), &salt)
        .map(|h| h.to_string())
        .map_err(|_| AppError::UnprocessableEntity("argon2 hash failed".into()))
}
