use rocket::http::Status;
use rocket::response::{Responder, Response};
use rocket::serde::json::json;
use std::io::Cursor;
use minio::s3::error::ErrorCode;
use thiserror::Error;

// I wanted to have a custom error type for the API,
// since I liked how Spring Boot handles errors. So I
// created a custom error type that implements the
// Responder trait. This is similar to what Rocket proposes
// and made me happy.

#[derive(Debug, Error)]
pub enum ApiError {
    #[error("Bad request: {0}")]
    BadRequest(String), // 400
    #[error("Forbidden")]
    Forbidden, //403
    #[error("Not found: {0}")]
    NotFound(String), //404
    #[error("Unprocessable entity: {0}")]
    UnprocessableEntity(String), //422
    #[error("Upstream storage error: {0}")]
    Storage(String), //424
    #[error("Internal error: {0}")]
    Internal(String), //500
}

impl ApiError {
    pub fn status(&self) -> Status {
        match self {
            ApiError::BadRequest(_) => Status::BadRequest,
            ApiError::Forbidden     => Status::Forbidden,
            ApiError::NotFound(_)     => Status::NotFound,
            ApiError::UnprocessableEntity(_) => Status::UnprocessableEntity,
            ApiError::Storage(_)    => Status::FailedDependency,
            ApiError::Internal(_)   => Status::InternalServerError,
        }
    }
}

impl From<minio::s3::error::Error> for ApiError {
    fn from(e: minio::s3::error::Error) -> Self {
        use minio::s3::error::Error::*;
        match &e {
            S3Error(se) if se.code == ErrorCode::NoSuchBucket => ApiError::NotFound("could not find bucket".to_owned() + &*se.bucket_name),
            S3Error(se) if se.code == ErrorCode::AccessDenied => ApiError::Forbidden,
            _ => ApiError::Storage(e.to_string()),
        }
    }
}

impl From<std::env::VarError> for ApiError {
    fn from(_: std::env::VarError) -> Self {
        ApiError::Internal("Missing/invalid environment variable".into())
    }
}

impl<'r> Responder<'r, 'static> for ApiError {
    fn respond_to(self, _req: &rocket::Request<'_>) -> rocket::response::Result<'static> {
        let body = json!({
            "error": self.to_string(),
            "status": self.status().code
        })
            .to_string();

        Response::build()
            .status(self.status())
            .header(rocket::http::ContentType::JSON)
            .sized_body(body.len(), Cursor::new(body))
            .ok()
    }
}

pub type ApiResult<T> = Result<T, ApiError>;


// ----------------------------------------------------
// Rocket error handlers - These are the pages that are shown
// when an error occurs in the application. I wanted to have
// JSON responses instead of HTML, so I used the JSON
// responder. All in the goal to be similar to Spring Boot
// ----------------------------------------------------

use rocket::{catch, catchers, Request};

#[catch(404)]
fn not_found(req: &Request<'_>) -> ApiError {
    ApiError::NotFound(req.uri().path().to_string())
}

#[catch(422)]
fn unprocessable_entity(req: &Request<'_>) -> ApiError {
    ApiError::UnprocessableEntity("body for ".to_owned() + &*req.uri().path().to_string() + " was invalid")
}

#[catch(default)]
fn default_catcher(status: Status, req: &Request<'_>) -> ApiError {
    match status.code {
        500 => ApiError::Internal("Internal server error".into()),
        424 => ApiError::Storage("Upstream storage error".into()),
        422 => ApiError::UnprocessableEntity("request for".to_owned() + &*req.uri().path().to_string() + " was invalid"),
        404 => ApiError::NotFound(req.uri().path().to_string()),
        403 => ApiError::Forbidden,
        400 => ApiError::BadRequest("Bad request".into()),
        _   => ApiError::Internal("Unhandled server error".into()),
    }
}


pub fn register() -> Vec<rocket::Catcher> {
    catchers![not_found, unprocessable_entity, default_catcher]
}
