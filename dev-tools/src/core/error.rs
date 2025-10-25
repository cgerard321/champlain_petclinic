use rocket::http::{ContentType, Status};
use rocket::response::Responder;
use rocket::serde::Serialize;
use rocket::serde::json::to_string;
use rocket::{Request, Response};
use std::io::Cursor;
use thiserror::Error;

// The application error type we handle
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
    #[error("Unprocessable entity: {0}")]
    UnprocessableEntity(String),
    #[error("Dependency failed")]
    FailedDependency,
    #[error("Internal error")]
    Internal
}

// The JSON error response
impl AppError {
    pub fn status(&self) -> Status {
        match self {
            AppError::BadRequest(_) => Status::BadRequest,
            AppError::Unauthorized => Status::Unauthorized,
            AppError::Forbidden => Status::Forbidden,
            AppError::NotFound(_) => Status::NotFound,
            AppError::UnprocessableEntity(_) => Status::UnprocessableEntity,
            AppError::FailedDependency => Status::FailedDependency,
            AppError::Internal => Status::InternalServerError,
        }
    }
    pub fn reason(&self) -> &'static str {
        match self {
            AppError::BadRequest(_) => "bad_request",
            AppError::Unauthorized => "unauthorized",
            AppError::Forbidden => "forbidden",
            AppError::NotFound(_) => "not_found",
            AppError::UnprocessableEntity(_) => "unprocessable_entity",
            AppError::FailedDependency => "dependency_failed",
            AppError::Internal => "internal_error",
        }
    }

    pub fn message(&self) -> String {
        match self {
            AppError::BadRequest(m) | AppError::NotFound(m) | AppError::UnprocessableEntity(m) => {
                m.clone()
            },
            AppError::Unauthorized => "You are not authorized to access this resource".into(),           
            AppError::Forbidden => "You don't have access to this resource".into(),
            AppError::FailedDependency => "A dependent service failed".into(),
            AppError::Internal => "An unexpected error occurred".into(),
        }
    }
}

#[derive(Serialize)]
struct ErrorBody<'a> {
    code: u16,
    reason: &'a str,
    message: String,
    path: Option<String>,
}

impl<'r> Responder<'r, 'static> for AppError {
    fn respond_to(self, req: &Request<'_>) -> rocket::response::Result<'static> {
        let status = self.status();
        let body = ErrorBody {
            code: status.code,
            reason: self.reason(),
            message: self.message(),
            path: Some(req.uri().path().to_string()),
        };
        let json = to_string(&body).unwrap_or_else(|_| "{\"code\":500}".to_string());
        Response::build()
            .status(status)
            .header(ContentType::JSON)
            .sized_body(json.len(), Cursor::new(json))
            .ok()
    }
}

pub type AppResult<T> = Result<T, AppError>;
