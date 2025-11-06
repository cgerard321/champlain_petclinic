use crate::shared::error::AppError;

pub fn get_pepper() -> String {
    std::env::var("PASSWORD_PEPPER")
        .map_err(|_| AppError::Internal)
        .expect("Missing password pepper")
}
