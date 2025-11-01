use crate::adapters::input::rest_handler::auth_guard::AuthenticatedUser;
use crate::adapters::input::rest_handler::docker_routes::utils::ws_logs_for_container;
use crate::application::ports::input::docker_logs_port::DynDockerPort;
use crate::core::error::AppResult;
use rocket::State;
use rocket_ws::{Channel, WebSocket};

#[get("/<service>/actions/fetch/logs/tail?<number_of_lines>")]
pub fn service_logs(
    user: AuthenticatedUser,
    ws: WebSocket,
    docker: &State<DynDockerPort>,
    service: String,
    number_of_lines: Option<usize>,
) -> AppResult<Channel<'static>> {
    let auth_context = user.into();
    ws_logs_for_container(ws, docker, service, number_of_lines, auth_context)
}

#[post("/<service>/actions/restart")]
pub async fn restart_service_container(
    user: AuthenticatedUser,
    docker: &State<DynDockerPort>,
    service: String,
) -> AppResult<()> {
    let auth_context = user.into();
    docker.restart_container(service, auth_context).await
}
