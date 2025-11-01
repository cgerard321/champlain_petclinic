use crate::adapters::input::rest_handler::auth_guard::{require_any, AuthenticatedUser};
use crate::adapters::input::rest_handler::docker_routes::utils::ws_logs_for_container;
use crate::application::ports::input::docker_logs_port::DynDockerPort;
use crate::core::config::{ADMIN_ROLE_UUID, SUDO_ROLE_UUID};
use crate::core::error::AppResult;
use rocket::State;
use rocket_ws::{Channel, WebSocket};

#[get("/portainer/actions/tail?<number_of_lines>")]
pub fn portainer_service_logs(
    user: AuthenticatedUser,
    ws: WebSocket,
    docker: &State<DynDockerPort>,
    number_of_lines: Option<usize>,
) -> AppResult<Channel<'static>> {
    require_any(&user, &[ADMIN_ROLE_UUID, SUDO_ROLE_UUID])?;

    ws_logs_for_container(ws, docker, "portainer", number_of_lines)
}
