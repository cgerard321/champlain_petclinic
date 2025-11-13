use crate::shared::error::{AppError, AppResult};
use argon2::{Argon2, PasswordHash, PasswordVerifier};

pub fn verify_password(stored_hash_bytes: &[u8], candidate: &str, pepper: &str) -> AppResult<bool> {
    let stored = std::str::from_utf8(stored_hash_bytes)
        .map_err(|_| AppError::BadRequest("invalid stored hash bytes".into()))?;
    let parsed = PasswordHash::new(stored)
        .map_err(|_| AppError::BadRequest("invalid stored hash format".into()))?;
    let argon = Argon2::default();
    let cand = format!("{candidate}{pepper}");
    Ok(argon.verify_password(cand.as_bytes(), &parsed).is_ok())
}
