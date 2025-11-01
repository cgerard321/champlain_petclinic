use crate::core::error::AppError;
use bollard::errors::Error;

pub fn map_docker_error(error: Error) -> AppError {
    log::info!("Docker error: {}", error);
    match error {
        _ => AppError::Internal,
    }
}
