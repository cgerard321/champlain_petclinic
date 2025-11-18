use crate::application::ports::output::crypto_port::CryptoPort;
use crate::shared::error::{AppError, AppResult};
use argon2::password_hash::rand_core::OsRng;
use argon2::password_hash::SaltString;
use argon2::{Argon2, PasswordHash, PasswordHasher, PasswordVerifier};

pub struct CryptoFunctions;

impl CryptoFunctions {
    pub fn new() -> Self {
        Self {}
    }
}

impl CryptoPort for CryptoFunctions {
    fn hash(&self, plain: &str) -> AppResult<String> {
        let pepper = get_pepper();
        let salt = SaltString::generate(&mut OsRng);
        let argon = Argon2::default();
        argon
            .hash_password(format!("{plain}{pepper}").as_bytes(), &salt)
            .map(|h| h.to_string())
            .map_err(|_| AppError::UnprocessableEntity("argon2 hash failed".into()))
    }

    fn verify_hash(&self, stored_hash_bytes: &[u8], candidate: &str) -> AppResult<bool> {
        let pepper = get_pepper();
        let stored = std::str::from_utf8(stored_hash_bytes)
            .map_err(|_| AppError::BadRequest("invalid stored hash bytes".into()))?;
        let parsed = PasswordHash::new(stored)
            .map_err(|_| AppError::BadRequest("invalid stored hash format".into()))?;
        let argon = Argon2::default();
        let cand = format!("{candidate}{pepper}");
        Ok(argon.verify_password(cand.as_bytes(), &parsed).is_ok())
    }
}

fn get_pepper() -> String {
    std::env::var("PASSWORD_PEPPER").unwrap_or_else(|_| "".into())
}
