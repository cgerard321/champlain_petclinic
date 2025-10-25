use crate::http::prelude::AppError;
use minio::s3::error::{Error as MinioError, ErrorCode};

impl From<MinioError> for AppError {
    fn from(e: MinioError) -> Self {
        match e {
            MinioError::S3Error(se) => match se.code {
                ErrorCode::NoSuchBucket => {
                    AppError::NotFound(format!("could not find bucket {}", se.bucket_name))
                }
                ErrorCode::AccessDenied => AppError::Forbidden,
                _ => AppError::FailedDependency,
            },
            MinioError::InvalidBucketName(name) => {
                AppError::BadRequest(format!("invalid bucket name: {}", name))
            }
            _ => AppError::Internal,
        }
    }
}
