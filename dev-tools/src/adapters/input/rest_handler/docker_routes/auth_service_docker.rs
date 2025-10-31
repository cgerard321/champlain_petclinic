use crate::adapters::input::rest_handler::auth_guard::{
    require_all, require_any, AuthenticatedUser,
};
use crate::adapters::input::rest_handler::docker_routes::utils::ws_logs_for_container;
use crate::application::ports::input::docker_logs_port::DynDockerPort;
use crate::core::config::{
    ADMIN_ROLE_UUID, AUTH_SERVICE_DEV_ROLE, EDITOR_ROLE_UUID, READER_ROLE_UUID,
};
use crate::core::error::AppResult;
use rocket::State;
use rocket_ws::{Channel, WebSocket};

#[get("/docker/logs/ws/auth-service?<number_of_lines>")]
pub fn auth_service_logs(
    user: AuthenticatedUser,
    ws: WebSocket,
    docker: &State<DynDockerPort>,
    number_of_lines: Option<usize>,
) -> AppResult<Channel<'static>> {
    require_any(&user, &[ADMIN_ROLE_UUID, AUTH_SERVICE_DEV_ROLE])?;
    require_all(&user, &[READER_ROLE_UUID])?;
    ws_logs_for_container(ws, docker, "auth-service", number_of_lines)
}

#[get("/docker/logs/ws/auth-service-db?<number_of_lines>")]
pub fn auth_service_db_logs(
    user: AuthenticatedUser,
    ws: WebSocket,
    docker: &State<DynDockerPort>,
    number_of_lines: Option<usize>,
) -> AppResult<Channel<'static>> {
    require_any(&user, &[ADMIN_ROLE_UUID, AUTH_SERVICE_DEV_ROLE])?;
    require_all(&user, &[READER_ROLE_UUID])?;
    ws_logs_for_container(ws, docker, "mysql-auth", number_of_lines)
}

#[post("/docker/auth-service/restart")]
pub async fn restart_auth_service_container(
    user: AuthenticatedUser,
    docker: &State<DynDockerPort>,
) -> AppResult<()> {
    require_any(&user, &[ADMIN_ROLE_UUID, AUTH_SERVICE_DEV_ROLE])?;
    require_all(&user, &[EDITOR_ROLE_UUID])?;

    docker.restart_container("auth-service").await
}

#[post("/docker/auth-service-db/restart")]
pub async fn restart_auth_service_db_container(
    user: AuthenticatedUser,
    docker: &State<DynDockerPort>,
) -> AppResult<()> {
    require_any(&user, &[ADMIN_ROLE_UUID, AUTH_SERVICE_DEV_ROLE])?;
    require_all(&user, &[EDITOR_ROLE_UUID])?;

    docker.restart_container("mysql-auth").await
}
