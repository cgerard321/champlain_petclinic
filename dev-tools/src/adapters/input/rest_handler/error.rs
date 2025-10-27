use crate::core::error::AppError;
use http::StatusCode;
use rocket::http::Status;
use rocket::{catch, catchers, Request};

use rocket::http::ContentType;
use rocket::response::Responder;
use rocket::serde::json::to_string;
use rocket::serde::Serialize;
use rocket::Response;
use std::io::Cursor;

impl AppError {
    // HTTP-specific mapping lives in the adapter
    pub fn http_status(&self) -> Status {
        match self {
            AppError::BadRequest(_) => Status::BadRequest,
            AppError::Unauthorized => Status::Unauthorized,
            AppError::Forbidden => Status::Forbidden,
            AppError::NotFound(_) => Status::NotFound,
            AppError::Conflict => Status::Conflict,
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
            AppError::Conflict => "conflict",
            AppError::UnprocessableEntity(_) => "unprocessable_entity",
            AppError::FailedDependency => "dependency_failed",
            AppError::Internal => "internal_error",
        }
    }

    pub fn message(&self) -> String {
        match self {
            AppError::BadRequest(m) | AppError::NotFound(m) | AppError::UnprocessableEntity(m) => {
                m.clone()
            }
            AppError::Unauthorized => "You are not authorized to access this resource".into(),
            AppError::Forbidden => "You don't have access to this resource".into(),
            AppError::Conflict => "A resource with this name already exists".into(),
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
        let status = self.http_status();
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

#[catch(404)]
fn not_found(req: &Request<'_>) -> AppError {
    AppError::NotFound(format!("Resource not found: {}", req.uri().path()))
}
#[catch(422)]
fn unprocessable(req: &Request<'_>) -> AppError {
    AppError::UnprocessableEntity(format!(
        "Validation failed for request to {}",
        req.uri().path()
    ))
}

#[catch(default)]
fn default_catcher(status: Status, _req: &Request<'_>) -> AppError {
    match StatusCode::from_u16(status.code).unwrap_or(StatusCode::INTERNAL_SERVER_ERROR) {
        StatusCode::BAD_REQUEST => AppError::BadRequest("Bad request".into()),
        StatusCode::UNAUTHORIZED => AppError::Unauthorized,
        StatusCode::FORBIDDEN => AppError::Forbidden,
        StatusCode::NOT_FOUND => AppError::NotFound("Resource not found".into()),
        StatusCode::UNPROCESSABLE_ENTITY => {
            AppError::UnprocessableEntity("Invalid request body".into())
        }
        StatusCode::FAILED_DEPENDENCY => AppError::FailedDependency,
        _ => AppError::Internal,
    }
}

pub fn register_catchers() -> Vec<rocket::Catcher> {
    catchers![not_found, unprocessable, default_catcher]
}
