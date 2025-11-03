use crate::adapters::input::rest_handler::contracts::docker_contracts::docker::LogResponseContract;
use crate::domain::entities::docker::DockerLogEntity;

impl From<DockerLogEntity> for LogResponseContract {
    fn from(entity: DockerLogEntity) -> Self {
        Self {
            type_name: entity.type_name,
            message: entity.message,
        }
    }
}
