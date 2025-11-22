use crate::application::ports::output::docker_api_port::DynDockerAPI;
use crate::application::services::docker::params::ViewLogsParams;
use crate::domain::entities::docker::DockerLogEntity;
use crate::domain::entities::service::ServiceEntity;
use crate::shared::error::{AppError, AppResult};
use futures::Stream;
use std::pin::Pin;

pub async fn stream_container_logs(
    docker_api: &DynDockerAPI,
    view_logs_params: ViewLogsParams,
    descriptor: &ServiceEntity,
) -> AppResult<Pin<Box<dyn Stream<Item=Result<DockerLogEntity, AppError>> + Send>>> {
    let db = descriptor
        .get_db_by_name_or_default(view_logs_params.db_name.as_ref().map(|s| s.to_string()))?;

    let service_name: String = if view_logs_params.container_type == "db" {
        db.db_host.to_string()
    } else {
        descriptor.docker_service.to_string()
    };

    docker_api
        .stream_container_logs(&service_name, view_logs_params.number_of_lines)
        .await
}
