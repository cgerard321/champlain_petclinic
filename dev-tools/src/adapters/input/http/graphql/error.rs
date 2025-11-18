use crate::shared::error::AppError;
use async_graphql::{Error, ErrorExtensions};

impl ErrorExtensions for AppError {
    fn extend(&self) -> Error {
        Error::new(self.to_string()).extend_with(|_err, eobj| {
            eobj.set(
                "code",
                match self {
                    AppError::BadRequest(_) => "BAD_REQUEST",
                    AppError::Unauthorized => "UNAUTHORIZED",
                    AppError::Forbidden => "FORBIDDEN",
                    AppError::NotFound(_) => "NOT_FOUND",
                    AppError::Conflict => "CONFLICT",
                    AppError::UnprocessableEntity(_) => "UNPROCESSABLE_ENTITY",
                    AppError::FailedDependency => "FAILED_DEPENDENCY",
                    AppError::Internal => "INTERNAL",
                    AppError::GatewayTimeout => "GATEWAY_TIMEOUT",
                },
            );
        })
    }
}
