use crate::core::error::AppError;
use minio::s3::error::Error::{InvalidBucketName, S3Error};
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
            InvalidBucketName(bn) => AppError::BadRequest(format!("invalid bucket name {0}", bn)),
            _ => AppError::FailedDependency,
        }
    }
}
