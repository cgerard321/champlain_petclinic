use crate::application::ports::output::docker_api_port::DynDockerAPI;
use crate::domain::entities::service::ServiceEntity;
use crate::shared::error::AppResult;

pub async fn restart_container(
    docker_api: &DynDockerAPI,
    descriptor: &ServiceEntity,
    container_type: &str,
    db_name: Option<&str>,
) -> AppResult<()> {
    let db = descriptor.get_db_by_name_or_default(db_name.map(|s| s.to_string()))?;

    let container_name: String = if container_type == "db" {
        db.db_host.to_string()
    } else {
        descriptor.docker_service.to_string()
    };

    docker_api.restart_container(&container_name).await
}
