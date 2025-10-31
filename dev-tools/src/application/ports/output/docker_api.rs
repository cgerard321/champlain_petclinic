use crate::core::error::{AppError, AppResult};
use crate::domain::entities::docker::DockerLogEntity;
use futures::Stream;
use std::pin::Pin;
use std::sync::Arc;

#[async_trait::async_trait]
pub trait DockerAPIPort: Send + Sync {
    async fn stream_container_logs(
        &self,
        container_name: &str,
        number_of_lines: Option<usize>,
    ) -> AppResult<Pin<Box<dyn Stream<Item = Result<DockerLogEntity, AppError>> + Send>>>;

    async fn restart_container(&self, container_id: &str) -> AppResult<()>;
}

pub type DynDockerAPI = Arc<dyn DockerAPIPort>;
