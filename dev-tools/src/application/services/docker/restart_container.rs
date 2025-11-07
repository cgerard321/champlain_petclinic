use crate::application::ports::output::docker_api::DynDockerAPI;
use crate::application::services::docker::utils::ServiceDescriptor;
use crate::shared::error::AppResult;

pub async fn restart_container(docker_api: &DynDockerAPI, descriptor: &ServiceDescriptor, container_type : &str) -> AppResult<()> {
    let container_name: String = if container_type == "db" {
        descriptor.docker_db.to_string()
    } else {
        descriptor.docker_service.to_string()
    };

    docker_api.restart_container(&container_name).await
}