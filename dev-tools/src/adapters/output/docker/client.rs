use crate::application::ports::output::docker_api::DockerAPIPort;
use crate::core::error::{AppError, AppResult};
use crate::domain::entities::docker::DockerLogEntity;
use bollard::query_parameters::{ListContainersOptions, LogsOptions};
use bollard::Docker;
use futures::{Stream, TryStreamExt};
use std::pin::Pin;

pub struct BollardDockerAPI {}

impl BollardDockerAPI {
    pub fn new() -> Self {
        Self {}
    }

    async fn resolve_id(docker: &Docker, target: &str) -> Result<String, AppError> {
        log::info!("Resolving container ID for {}", target);

        let list = docker
            .list_containers(Some(ListContainersOptions {
                all: true,
                ..Default::default()
            }))
            .await
            .map_err(|e| {
                log::error!("list_containers: {e}");
                AppError::Internal
            })?;

        log::info!("Found {} containers", list.len());

        if let Some(c) = list.into_iter().next() {
            log::info!("Found container: {:?}", c.id);
            return Ok(c.id.unwrap_or_default());
        }

        log::info!("No containers found");

        Err(AppError::NotFound(format!(
            "Container not found: {}",
            target
        )))
    }
}

#[async_trait]
impl DockerAPIPort for BollardDockerAPI {
    async fn stream_container_logs(
        &self,
        container_name: &str,
    ) -> AppResult<Pin<Box<dyn Stream<Item = Result<DockerLogEntity, AppError>> + Send>>> {
        log::info!("Streaming logs for container: {}", container_name);

        let docker = Docker::connect_with_unix_defaults().map_err(|e| {
            log::error!("Failed to connect to Docker socket: {e}");
            AppError::Internal
        })?;

        log::info!("Connected to Docker");

        let id = BollardDockerAPI::resolve_id(&docker, container_name)
            .await
            .map_err(|e| {
                match &e {
                    AppError::NotFound(msg) => log::warn!("{msg}"),
                    _ => log::error!("resolve_id error: {e}"),
                }
                e
            })?;

        let log_stream = docker
            .logs(
                id.as_str(),
                Some(LogsOptions {
                    follow: true,
                    stdout: true,
                    stderr: true,
                    timestamps: true,
                    tail: "10".into(),
                    ..Default::default()
                }),
            )
            .map_ok(DockerLogEntity::from)
            .map_err(|e| {
                log::error!("Error streaming logs: {e}");
                AppError::Internal
            });

        Ok(Box::pin(log_stream))
    }
}
