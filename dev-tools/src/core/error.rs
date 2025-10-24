use thiserror::Error;

#[derive(Debug, Error)]
pub enum AppError {
    #[error("Bad request: {0}")]
    BadRequest(String),
    #[error("Forbidden")]
    Forbidden,
    #[error("Not found: {0}")]
    NotFound(String),
    #[error("Unprocessable entity: {0}")]
    UnprocessableEntity(String),
    #[error("Dependency failed")]
    Dependency, // generic “upstream” failure
    #[error("Internal error")]
    Internal,
}

pub type AppResult<T> = Result<T, AppError>;

impl From<std::env::VarError> for AppError {
    fn from(_: std::env::VarError) -> Self {
        AppError::Internal
    }
}
