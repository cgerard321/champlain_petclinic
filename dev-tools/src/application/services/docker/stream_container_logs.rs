use crate::application::ports::output::docker_api_port::DynDockerAPI;
use crate::application::services::docker::params::ViewLogsParams;
use crate::application::services::utils::ServiceDescriptor;
use crate::domain::entities::docker::DockerLogEntity;
use crate::shared::error::{AppError, AppResult};
use futures::Stream;
use std::pin::Pin;

pub async fn stream_container_logs(
    docker_api: &DynDockerAPI,
    view_logs_params: ViewLogsParams,
    descriptor: &ServiceDescriptor,
) -> AppResult<Pin<Box<dyn Stream<Item=Result<DockerLogEntity, AppError>> + Send>>> {
    let service_name: String = if view_logs_params.container_type == "db" {
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

    docker_api
        .stream_container_logs(&service_name, view_logs_params.number_of_lines)
        .await
}
