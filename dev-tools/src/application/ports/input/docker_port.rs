use crate::application::services::docker::params::{RestartContainerParams, ViewLogsParams};
use crate::application::services::user_context::UserContext;
use crate::domain::entities::docker::DockerLogEntity;
use crate::shared::error::{AppError, AppResult};
use futures::Stream;
use std::pin::Pin;
use crate::application::services::docker::projections::ServiceProjection;

#[async_trait::async_trait]
pub trait DockerPort: Send + Sync {
    async fn stream_container_logs(
        &self,
        view_logs_params: ViewLogsParams,
        user_ctx: UserContext,
    ) -> AppResult<Pin<Box<dyn Stream<Item=Result<DockerLogEntity, AppError>> + Send>>>;
    async fn restart_container(
        &self,
        restart_params: RestartContainerParams,
        user_ctx: UserContext,
    ) -> AppResult<()>;

    async fn container_list(&self, user_ctx: UserContext) -> AppResult<Vec<ServiceProjection>>;
}
pub type DynDockerPort = std::sync::Arc<dyn DockerPort>;
