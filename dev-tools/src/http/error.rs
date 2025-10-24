use crate::core::error::AppError;
use rocket::http::Status;
use rocket::{Request, catch, catchers};

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
