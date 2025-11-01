use crate::application::ports::output::docker_api::DockerAPIPort;
use crate::core::error::{AppError, AppResult};
use crate::domain::entities::docker::DockerLogEntity;
use bollard::query_parameters::{ListContainersOptions, LogsOptions, RestartContainerOptions};
use bollard::Docker;
use futures::{Stream, TryStreamExt};
use std::collections::HashMap;
use std::pin::Pin;

pub struct BollardDockerAPI {}

impl BollardDockerAPI {
    pub fn new() -> Self {
        Self {}
    }

    async fn resolve_id(docker: &Docker, target_name: &str) -> Result<String, AppError> {
        log::info!("Resolving container ID for {}", target_name);

        let mut filters: HashMap<String, Vec<String>> = HashMap::new();
        filters.insert("name".to_string(), vec![target_name.to_string()]);

        let list = docker
            .list_containers(Some(ListContainersOptions {
                all: true,
                filters: Option::from(filters),
                ..Default::default()
            }))
            .await
            .map_err(|e| {
                log::error!("list_containers: {e}");
                AppError::Internal
            })?;

        log::info!("Found {} containers", list.len());

        // We expect only one container to match the name so we take the first one
        // if we have more, we just ignore the rest since that is probably an
        // orphan container. If we ever do scale the container count, we will
        // need to handle this properly.
        // We sort by length and take the shortest one.
        if let Some(container) = list
            .into_iter()
            .min_by_key(|c| {
                c.names
                    .as_ref()
                    .and_then(|names| names.first())
                    .map(|name| name.len())
                    .unwrap_or(usize::MAX)
            })
        {
            let container_id = container.id.unwrap_or_default();
            return Ok(container_id);
        }

        log::info!("No containers found");

        Err(AppError::NotFound(format!(
            "Container not found: {}",
            target_name
        )))
    }
}

#[async_trait]
impl DockerAPIPort for BollardDockerAPI {
    async fn stream_container_logs(
        &self,
        container_name: &str,
        number_of_lines: Option<usize>,
    ) -> AppResult<Pin<Box<dyn Stream<Item = Result<DockerLogEntity, AppError>> + Send>>> {
        log::info!("Streaming logs for container: {}", container_name);

        let docker = Docker::connect_with_unix_defaults().map_err(|e| {
            log::error!("Failed to connect to Docker socket: {e}");
            AppError::Internal
        })?;

        log::info!("Connected to Docker");

        let id = BollardDockerAPI::resolve_id(&docker, container_name).await?;

        let number_of_lines = number_of_lines.unwrap_or(10).to_string();

        let log_stream = docker
            .logs(
                id.as_str(),
                Some(LogsOptions {
                    follow: true,
                    stdout: true,
                    stderr: true,
                    timestamps: true,
                    tail: number_of_lines,
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

    async fn restart_container(&self, container_name: &str) -> AppResult<()> {
        let docker = Docker::connect_with_unix_defaults().map_err(|e| {
            log::error!("Failed to connect to Docker socket: {e}");
            AppError::Internal
        })?;

        let id = BollardDockerAPI::resolve_id(&docker, container_name).await?;

        // If we want to pass options to the restart, we can do it like this:
        // let options = Some(RestartContainerOptions {
        //     ..Default::default()
        // });
        // For now, we just use the default options.

        Ok(docker
            .restart_container(id.as_str(), None::<RestartContainerOptions>)
            .await
            .map_err(|e| {
                log::error!("Error restarting container: {e}");
                AppError::Internal
            })?)
    }
}
