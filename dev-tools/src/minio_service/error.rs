use crate::http::prelude::AppError;
use minio::s3::error::Error::S3Error;
use minio::s3::error::{Error as MinioError, ErrorCode};

impl From<MinioError> for AppError {
    fn from(e: MinioError) -> Self {
        match e {
            S3Error(se) => match se.code {
                ErrorCode::NoSuchBucket => {
                    AppError::NotFound(format!("could not find bucket {0}", se.bucket_name))
                }
                ErrorCode::AccessDenied => AppError::Forbidden,
                _ => AppError::FailedDependency,
            },
            _ => AppError::FailedDependency,
        }
    }
}
