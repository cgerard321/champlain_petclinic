use crate::application::services::auth_context::AuthContext;
use crate::core::error::AppError;
use uuid::Uuid;

#[inline]
pub fn require_any(user: &AuthContext, required: &[Uuid]) -> Result<(), AppError> {
    if required.iter().any(|r| user.roles.contains(r)) {
        Ok(())
    } else {
        Err(AppError::Forbidden)
    }
}

#[inline]
pub fn require_all(user: &AuthContext, required: &[Uuid]) -> Result<(), AppError> {
    if required.iter().all(|r| user.roles.contains(r)) {
        Ok(())
    } else {
        Err(AppError::Forbidden)
    }
}
