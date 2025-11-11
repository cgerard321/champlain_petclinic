use crate::application::ports::input::docker_logs_port::DockerPort;
use crate::application::ports::output::docker_api::DynDockerAPI;
use crate::application::services::docker::params::{RestartContainerParams, ViewLogsParams};
use crate::application::services::docker::restart_container::restart_container;
use crate::application::services::docker::stream_container_logs::stream_container_logs;
use crate::application::services::user_context::{
    UserContext, require_all, require_any, verify_service_or_admin_perms,
};
use crate::application::services::utils::resolve_descriptor_by_container;
use crate::domain::entities::docker::DockerLogEntity;
use crate::shared::config::{ADMIN_ROLE_UUID, EDITOR_ROLE_UUID, READER_ROLE_UUID};
use crate::shared::error::{AppError, AppResult};
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
        view_logs_params: ViewLogsParams,
        user_ctx: UserContext,
    ) -> AppResult<Pin<Box<dyn Stream<Item = Result<DockerLogEntity, AppError>> + Send>>> {
        log::info!(
            "Streaming logs for container: {}",
            view_logs_params.container_name
        );

        let desc =
            resolve_descriptor_by_container(&view_logs_params.container_name).ok_or_else(|| {
                log::info!("Unknown container '{}'", view_logs_params.container_name);
                AppError::NotFound(format!(
                    "Unknown container '{}'",
                    view_logs_params.container_name
                ))
            })?;

        log::info!("Resolved descriptor: {:?}", desc);
        verify_service_or_admin_perms(&user_ctx, desc)?;
        log::info!("Access granted");

        stream_container_logs(&self.docker_api, view_logs_params, desc).await
    }

    async fn restart_container(
        &self,
        restart_params: RestartContainerParams,
        user_ctx: UserContext,
    ) -> AppResult<()> {
        let container_name = restart_params.container_name;
        let container_type = restart_params.container_type;
        log::info!("Restarting container: {}", container_name);

        let desc = resolve_descriptor_by_container(&container_name).ok_or_else(|| {
            AppError::BadRequest(format!("Unknown container '{}'", container_name))
        })?;

        log::info!("Resolved descriptor: {:?}", desc);
        verify_service_or_admin_perms(&user_ctx, desc)?;
        log::info!("Access granted");

        restart_container(&self.docker_api, desc, &container_type).await
    }
}
