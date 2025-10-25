use crate::core::error::{AppError, AppResult};
use uuid::Uuid;

pub fn safe_string_to_uuid(s: &str) -> AppResult<Uuid> {
    Uuid::parse_str(s)
        .map_err(|_| AppError::UnprocessableEntity(format!("Invalid UUID format: {}", s)))
}
