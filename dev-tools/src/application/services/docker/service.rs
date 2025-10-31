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
    async fn stream_auth_service_logs(
        &self,
    ) -> AppResult<Pin<Box<dyn Stream<Item = Result<DockerLogEntity, AppError>> + Send>>> {
        self.docker_api.stream_container_logs("auth_service").await
    }

    async fn stop_container(&self, container_id: &str) -> AppResult<()> {
        todo!()
    }

    async fn start_container(&self, container_id: &str) -> AppResult<()> {
        todo!()
    }

    async fn restart_container(&self, container_id: &str) -> AppResult<()> {
        todo!()
    }
}
