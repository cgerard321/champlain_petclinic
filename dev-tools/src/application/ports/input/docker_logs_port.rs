use crate::core::error::{AppError, AppResult};
use crate::domain::entities::docker::DockerLogEntity;
use futures::Stream;
use std::pin::Pin;

#[allow(dead_code)]
#[async_trait::async_trait]
pub trait DockerPort: Send + Sync {
    async fn stream_auth_service_logs(
        &self,
    ) -> AppResult<Pin<Box<dyn Stream<Item = Result<DockerLogEntity, AppError>> + Send>>>;
    async fn stop_container(&self, container_id: &str) -> AppResult<()>;
    async fn start_container(&self, container_id: &str) -> AppResult<()>;
    async fn restart_container(&self, container_id: &str) -> AppResult<()>;
}
pub type DynDockerPort = std::sync::Arc<dyn DockerPort>;
