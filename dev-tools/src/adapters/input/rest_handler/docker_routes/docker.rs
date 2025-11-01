use crate::adapters::input::rest_handler::auth_guard::AuthenticatedUser;
use crate::adapters::input::rest_handler::contracts::docker_contracts::docker::ContainerActionRequestContract;
use crate::adapters::input::rest_handler::docker_routes::utils::ws_logs_for_container;
use crate::application::ports::input::docker_logs_port::DynDockerPort;
use crate::application::services::docker::params::RestartContainerParams;
use crate::core::error::AppResult;
use rocket::serde::json::Json;
use rocket::State;
use rocket_ws::{Channel, WebSocket};

#[get("/<service>/actions/fetch/logs/tail?<container_type>&<number_of_lines>")]
pub fn service_logs(
    user: AuthenticatedUser,
    ws: WebSocket,
    docker: &State<DynDockerPort>,
    service: String,
    container_type: Option<String>,
    number_of_lines: Option<usize>,
) -> AppResult<Channel<'static>> {
    let auth_context = user.into();

    let container_type = container_type.unwrap_or_else(|| "service".to_string());

    ws_logs_for_container(
        ws,
        docker,
        service,
        container_type,
        number_of_lines,
        auth_context,
    )
}

#[post(
    "/<service>/actions/restart",
    format = "application/json",
    data = "<restart_request>"
)]
pub async fn restart_service_container(
    user: AuthenticatedUser,
    docker: &State<DynDockerPort>,
    service: String,
    restart_request: Json<ContainerActionRequestContract>,
) -> AppResult<()> {
    let auth_context = user.into();
    let restart_params = RestartContainerParams {
        container_name: service,
        container_type: restart_request.container_type.clone(),
    };

    docker.restart_container(restart_params, auth_context).await
}
