use crate::core::error::AppError;

pub fn get_pepper() -> Result<String, AppError> {
    std::env::var("PASSWORD_PEPPER").map_err(|_| AppError::Internal) // or AppError::FailedDependency
}
