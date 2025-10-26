use thiserror::Error;

#[derive(Debug, Error)]
pub enum AppError {
    #[error("Bad request: {0}")]
    BadRequest(String),
    #[error("Unauthorized")]
    Unauthorized,
    #[error("Forbidden")]
    Forbidden,
    #[error("Not found: {0}")]
    NotFound(String),
    #[error("Conflict")]
    Conflict,
    #[error("Unprocessable entity: {0}")]
    UnprocessableEntity(String),
    #[error("Dependency failed")]
    FailedDependency,
    #[error("Internal error")]
    Internal,
}

pub type AppResult<T> = Result<T, AppError>;
