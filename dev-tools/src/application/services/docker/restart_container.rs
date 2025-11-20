use crate::application::ports::output::docker_api_port::DynDockerAPI;
use crate::application::services::utils::ServiceDescriptor;
use crate::shared::error::{AppError, AppResult};

pub async fn restart_container(
    docker_api: &DynDockerAPI,
    descriptor: &ServiceDescriptor,
    container_type: &str,
) -> AppResult<()> {
    let container_name: String = if container_type == "db" {
        descriptor
            .db
            .as_ref()
            .ok_or_else(|| {
                AppError::NotFound(format!(
                    "Service '{}' does not have a database container",
                    descriptor.docker_service
                ))
            })?
            .db_host
            .to_string()
    } else {
        descriptor.docker_service.to_string()
    };

    docker_api.restart_container(&container_name).await
}
