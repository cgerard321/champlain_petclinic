use crate::core::error::AppError;
use http::StatusCode;
use rocket::http::Status;
use rocket::{Request, catch, catchers};

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
    match StatusCode::from_u16(status.code).unwrap() {
        StatusCode::BAD_REQUEST => AppError::BadRequest("Bad request".into()),
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
