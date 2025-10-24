use crate::shared::error::AppError;
use rocket::http::{ContentType, Status};
use rocket::response::{Responder, Response};
use rocket::serde::{Serialize, json::to_string};
use rocket::{Request, catch, catchers};
use std::io::Cursor;

impl AppError {
    pub fn status(&self) -> Status {
        match self {
            AppError::BadRequest(_) => Status::BadRequest,
            AppError::Forbidden => Status::Forbidden,
            AppError::NotFound(_) => Status::NotFound,
            AppError::UnprocessableEntity(_) => Status::UnprocessableEntity,
            AppError::Dependency => Status::FailedDependency,
            AppError::Internal => Status::InternalServerError,
        }
    }
    pub fn reason(&self) -> &'static str {
        match self {
            AppError::BadRequest(_) => "bad_request",
            AppError::Forbidden => "forbidden",
            AppError::NotFound(_) => "not_found",
            AppError::UnprocessableEntity(_) => "unprocessable_entity",
            AppError::Dependency => "dependency_failed",
            AppError::Internal => "internal_error",
        }
    }

    pub fn message(&self) -> String {
        match self {
            AppError::BadRequest(m) | AppError::NotFound(m) | AppError::UnprocessableEntity(m) => {
                m.clone()
            }
            AppError::Forbidden => "You don't have access to this resource".into(),
            AppError::Dependency => "A dependent service failed".into(),
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

#[catch(404)]
fn not_found(req: &Request<'_>) -> AppError {
    AppError::NotFound(format!("Resource not found: {}", req.uri().path()))
}
#[catch(422)]
fn unprocessable(req: &Request<'_>) -> AppError {
    AppError::UnprocessableEntity(format!("Invalid request body for {}", req.uri().path()))
}
#[catch(default)]
fn default_catcher(status: Status, _req: &Request<'_>) -> AppError {
    match status.code {
        400 => AppError::BadRequest("Bad request".into()),
        403 => AppError::Forbidden,
        404 => AppError::NotFound("Resource not found".into()),
        422 => AppError::UnprocessableEntity("Invalid request body".into()),
        424 => AppError::Dependency,
        _ => AppError::Internal,
    }
}

pub fn register_catchers() -> Vec<rocket::Catcher> {
    catchers![not_found, unprocessable, default_catcher]
}
