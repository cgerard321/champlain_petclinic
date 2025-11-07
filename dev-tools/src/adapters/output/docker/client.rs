use crate::adapters::output::docker::error::map_docker_error;
use crate::application::ports::output::docker_api::DockerAPIPort;
use crate::domain::entities::docker::DockerLogEntity;
use crate::shared::config::DEFAULT_TAIL_AMOUNT;
use crate::shared::error::{AppError, AppResult};
use bollard::query_parameters::{ListContainersOptions, LogsOptions, RestartContainerOptions};
use bollard::Docker;
use futures::{Stream, TryStreamExt};
use std::collections::HashMap;
use std::pin::Pin;

pub struct BollardDockerAPI {
    docker: Docker,
}

impl BollardDockerAPI {
    pub fn new(docker: Docker) -> Self {
        Self { docker }
    }

    async fn resolve_id(&self, target_name: &str) -> AppResult<String> {
        log::info!("Resolving container ID for {}", target_name);

        let mut filters: HashMap<String, Vec<String>> = HashMap::new();
        filters.insert("name".to_string(), vec![target_name.to_string()]);

        let list = self
            .docker
            .list_containers(Some(ListContainersOptions {
                all: true,
                filters: Option::from(filters),
                ..Default::default()
            }))
            .await
            .map_err(map_docker_error)?;

        log::info!("Found {} containers", list.len());

        // We expect only one container to match the name so we take the first one
        // if we have more, we just ignore the rest since that is probably an
        // orphan container. If we ever do scale the container count, we will
        // need to handle this properly.
        // We sort by length and take the shortest one.
        if let Some(container) = list.into_iter().min_by_key(|c| {
            c.names
                .as_ref()
                .and_then(|names| names.first())
                .map(|name| name.len())
                .unwrap_or(usize::MAX)
        }) {
            let container_id = container.id.ok_or_else(|| {
                log::error!("Container ID not found for container: {}", target_name);
                AppError::Internal
            })?;

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
    ) -> AppResult<Pin<Box<dyn Stream<Item = AppResult<DockerLogEntity>> + Send>>> {
        log::info!("Streaming logs for container: {}", container_name);

        log::info!("Connected to Docker");

        let id = BollardDockerAPI::resolve_id(self, container_name).await?;

        let number_of_lines = number_of_lines.unwrap_or(DEFAULT_TAIL_AMOUNT).to_string();

        let log_stream = self
            .docker
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
            .map_err(map_docker_error);

        Ok(Box::pin(log_stream))
    }

    async fn restart_container(&self, container_name: &str) -> AppResult<()> {
        let id = BollardDockerAPI::resolve_id(self, container_name).await?;

        // If we want to pass options to the restart, we can do it like this:
        // let options = Some(RestartContainerOptions {
        //     ..Default::default()
        // });
        // For now, we just use the default options.

        Ok(self
            .docker
            .restart_container(id.as_str(), None::<RestartContainerOptions>)
            .await
            .map_err(map_docker_error)?)
    }
}
