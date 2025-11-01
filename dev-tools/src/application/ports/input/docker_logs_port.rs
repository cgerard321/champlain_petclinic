use crate::application::services::auth_context::AuthContext;
use crate::application::services::docker::params::ViewLogsParams;
use crate::core::error::{AppError, AppResult};
use crate::domain::entities::docker::DockerLogEntity;
use futures::Stream;
use std::pin::Pin;

#[async_trait::async_trait]
pub trait DockerPort: Send + Sync {
    async fn stream_container_logs(
        &self,
        view_logs_params: ViewLogsParams,
        auth_context: AuthContext,
    ) -> AppResult<Pin<Box<dyn Stream<Item = Result<DockerLogEntity, AppError>> + Send>>>;
    async fn restart_container(
        &self,
        container_name: String,
        auth_context: AuthContext,
    ) -> AppResult<()>;
}
pub type DynDockerPort = std::sync::Arc<dyn DockerPort>;
