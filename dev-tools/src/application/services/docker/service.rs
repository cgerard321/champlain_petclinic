use crate::application::ports::input::docker_logs_port::DockerPort;
use crate::application::ports::output::docker_api::DynDockerAPI;
use crate::application::services::docker::params::{RestartContainerParams, ViewLogsParams};
use crate::application::services::docker::utils::{ServiceDescriptor, SERVICES};
use crate::application::services::user_context::{require_all, require_any, UserContext};
use crate::domain::entities::docker::DockerLogEntity;
use crate::shared::config::{ADMIN_ROLE_UUID, EDITOR_ROLE_UUID, READER_ROLE_UUID};
use crate::shared::error::{AppError, AppResult};
use futures::Stream;
use std::pin::Pin;
use crate::application::services::docker::resolve_descriptor_by_container::resolve_descriptor_by_container;
use crate::application::services::docker::restart_container::restart_container;
use crate::application::services::docker::stream_container_logs::stream_container_logs;

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
        auth_context: UserContext,
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

        if let Some(required_role) = desc.logs_role {
            log::info!("Verifying logs role:");
            require_all(&auth_context, &[READER_ROLE_UUID])?;
            require_any(&auth_context, &[ADMIN_ROLE_UUID, required_role])?;
        } else {
            log::info!("Verifying admin role:");
            require_any(&auth_context, &[ADMIN_ROLE_UUID])?;
        }
        log::info!("Access granted");

        stream_container_logs(
            &self.docker_api,
            view_logs_params,
            desc,
        ).await
    }

    async fn restart_container(
        &self,
        restart_params: RestartContainerParams,
        auth_context: UserContext,
    ) -> AppResult<()> {
        let container_name = restart_params.container_name;
        let container_type = restart_params.container_type;
        log::info!("Restarting container: {}", container_name);

        let desc = resolve_descriptor_by_container(&container_name).ok_or_else(|| {
            AppError::BadRequest(format!("Unknown container '{}'", container_name))
        })?;

        log::info!("Resolved descriptor: {:?}", desc);

        if let Some(required_role) = desc.restart_role {
            log::info!("Verifying restart role:");
            require_all(&auth_context, &[EDITOR_ROLE_UUID])?;
            require_any(&auth_context, &[ADMIN_ROLE_UUID, required_role])?;
        } else {
            log::info!("Verifying admin role:");
            require_all(&auth_context, &[ADMIN_ROLE_UUID])?;
        }

        restart_container(
            &self.docker_api,
            &desc,
            &container_type,
        ).await
    }
}

