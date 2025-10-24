use crate::http::prelude::AppError;
use minio::s3::error::ErrorCode;

impl From<minio::s3::error::Error> for AppError {
    fn from(e: minio::s3::error::Error) -> Self {
        use minio::s3::error::Error::*;
        match &e {
            S3Error(se) if se.code == ErrorCode::NoSuchBucket => {
                AppError::NotFound(format!("could not find bucket {}", se.bucket_name))
            }
            S3Error(se) if se.code == ErrorCode::AccessDenied => AppError::Forbidden,
            _ => AppError::Dependency,
        }
    }
}
