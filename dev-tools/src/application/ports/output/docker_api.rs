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
    ) -> AppResult<Pin<Box<dyn Stream<Item = Result<DockerLogEntity, AppError>> + Send>>>;
}

pub type DynDockerAPI = Arc<dyn DockerAPIPort>;
