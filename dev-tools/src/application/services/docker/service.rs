use crate::application::ports::input::docker_logs_port::DockerPort;
use crate::application::ports::output::docker_api::DynDockerAPI;
use crate::core::error::{AppError, AppResult};
use crate::domain::entities::docker::DockerLogEntity;
use futures::Stream;
use std::pin::Pin;

pub struct DockerService {
    pub docker_api: DynDockerAPI,
}

impl DockerService {
    pub fn new(docker_api: DynDockerAPI) -> Self {
        Self { docker_api }
    }
}

#[async_trait::async_trait]
impl DockerPort for DockerService {
    async fn stream_container_logs(
        &self,
        container_name: &str,
        number_of_lines: Option<usize>,
    ) -> AppResult<Pin<Box<dyn Stream<Item = Result<DockerLogEntity, AppError>> + Send>>> {
        self.docker_api
            .stream_container_logs(container_name, number_of_lines)
            .await
    }

    async fn restart_container(&self, container_name: &str) -> AppResult<()> {
        self.docker_api.restart_container(container_name).await
    }
}
