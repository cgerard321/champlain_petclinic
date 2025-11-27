use crate::application::ports::input::docker_port::DockerPort;
use crate::application::ports::output::docker_api_port::DynDockerAPI;
use crate::application::ports::output::services_repo_port::DynServicesRepo;
use crate::application::services::docker::params::{RestartContainerParams, ViewLogsParams};
use crate::application::services::docker::projections::ServiceProjection;
use crate::application::services::docker::restart_container::restart_container;
use crate::application::services::docker::stream_container_logs::stream_container_logs;
use crate::application::services::user_context::{verify_service_or_admin_perms, UserContext};
use crate::domain::entities::docker::DockerLogEntity;
use crate::shared::error::{AppError, AppResult};
use futures::Stream;
use std::pin::Pin;

pub struct DockerService {
    pub docker_api: DynDockerAPI,
    pub service_repo: DynServicesRepo,
}

impl DockerService {
    pub fn new(docker_api: DynDockerAPI, service_repo: DynServicesRepo) -> Self {
        Self {
            docker_api,
            service_repo,
        }
    }
}

#[async_trait::async_trait]
impl DockerPort for DockerService {
    async fn stream_container_logs(
        &self,
        view_logs_params: ViewLogsParams,
        user_ctx: UserContext,
    ) -> AppResult<Pin<Box<dyn Stream<Item=Result<DockerLogEntity, AppError>> + Send>>> {
        log::info!(
            "Streaming logs for container: {}",
            view_logs_params.container_name
        );

        let desc = self
            .service_repo
            .get_service(&view_logs_params.container_name)
            .await?;

        log::info!("Resolved descriptor: {:?}", desc);
        verify_service_or_admin_perms(&user_ctx, &desc)?;
        log::info!("Access granted");

        stream_container_logs(&self.docker_api, view_logs_params, &desc).await
    }

    async fn restart_container(
        &self,
        restart_params: RestartContainerParams,
        user_ctx: UserContext,
    ) -> AppResult<()> {
        let container_name = restart_params.container_name;
        let container_type = restart_params.container_type;
        let db_name = restart_params.db_name;
        log::info!("Restarting container: {}", container_name);

        let desc = self.service_repo.get_service(&container_name).await?;

        log::info!("Resolved descriptor: {:?}", desc);
        verify_service_or_admin_perms(&user_ctx, &desc)?;
        log::info!("Access granted");

        restart_container(&self.docker_api, &desc, &container_type, db_name.as_deref()).await
    }

    async fn container_list(&self, user_ctx: &UserContext) -> AppResult<Vec<ServiceProjection>> {
        let services = self.service_repo.list_services().await?;

        Ok(services
            .into_iter()
            .filter_map(|svc| {
                let perm_check = verify_service_or_admin_perms(user_ctx, &svc);
                match perm_check {
                    Ok(_) => Some(ServiceProjection::from_descriptor(&svc)),
                    Err(_) => None,
                }
            })
            .collect())
    }
}
