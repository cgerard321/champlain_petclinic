use crate::adapters::input::rest_handler::auth_guard::AuthenticatedUser;
use crate::adapters::input::rest_handler::docker_routes::utils::{
    ensure_logs_permissions, ensure_restart_permissions, ws_logs_for_container,
};
use crate::application::ports::input::docker_logs_port::DynDockerPort;
use crate::core::error::AppResult;
use rocket::State;
use rocket_ws::{Channel, WebSocket};

#[get("/containers/files-service/logs?<number_of_lines>")]
pub fn files_service_logs(
    user: AuthenticatedUser,
    ws: WebSocket,
    docker: &State<DynDockerPort>,
    number_of_lines: Option<usize>,
) -> AppResult<Channel<'static>> {
    ensure_logs_permissions(&user, None)?;

    ws_logs_for_container(ws, docker, "files-service", number_of_lines)
}

#[get("/containers/files-service-db/logs?<number_of_lines>")]
pub fn files_service_db_logs(
    user: AuthenticatedUser,
    ws: WebSocket,
    docker: &State<DynDockerPort>,
    number_of_lines: Option<usize>,
) -> AppResult<Channel<'static>> {
    ensure_logs_permissions(&user, None)?;

    ws_logs_for_container(ws, docker, "mysql-files", number_of_lines)
}

#[post("/containers/files-service/restart")]
pub async fn restart_files_service_container(
    user: AuthenticatedUser,
    docker: &State<DynDockerPort>,
) -> AppResult<()> {
    ensure_restart_permissions(&user, None)?;

    docker.restart_container("files-service").await
}

#[post("/containers/files-service-db/restart")]
pub async fn restart_files_service_db_container(
    user: AuthenticatedUser,
    docker: &State<DynDockerPort>,
) -> AppResult<()> {
    ensure_restart_permissions(&user, None)?;

    docker.restart_container("mysql-files").await
}
