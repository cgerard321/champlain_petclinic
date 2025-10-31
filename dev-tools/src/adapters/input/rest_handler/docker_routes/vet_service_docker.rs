use crate::adapters::input::rest_handler::auth_guard::AuthenticatedUser;
use crate::adapters::input::rest_handler::docker_routes::utils::{
    ensure_logs_permissions, ensure_restart_permissions, ws_logs_for_container,
};
use crate::application::ports::input::docker_logs_port::DynDockerPort;
use crate::core::config::VET_SERVICE_DEV_ROLE;
use crate::core::error::AppResult;
use rocket::State;
use rocket_ws::{Channel, WebSocket};

#[get("/containers/vet-service/logs?<number_of_lines>")]
pub fn vet_service_logs(
    user: AuthenticatedUser,
    ws: WebSocket,
    docker: &State<DynDockerPort>,
    number_of_lines: Option<usize>,
) -> AppResult<Channel<'static>> {
    ensure_logs_permissions(&user, Option::from(VET_SERVICE_DEV_ROLE))?;

    ws_logs_for_container(ws, docker, "vet-service", number_of_lines)
}

#[get("/containers/vet-service-db/logs?<number_of_lines>")]
pub fn vet_service_db_logs(
    user: AuthenticatedUser,
    ws: WebSocket,
    docker: &State<DynDockerPort>,
    number_of_lines: Option<usize>,
) -> AppResult<Channel<'static>> {
    ensure_logs_permissions(&user, Option::from(VET_SERVICE_DEV_ROLE))?;

    ws_logs_for_container(ws, docker, "mongo-vet", number_of_lines)
}

#[post("/containers/vet-service/restart")]
pub async fn restart_vet_service_container(
    user: AuthenticatedUser,
    docker: &State<DynDockerPort>,
) -> AppResult<()> {
    ensure_restart_permissions(&user, Option::from(VET_SERVICE_DEV_ROLE))?;

    docker.restart_container("vet-service").await
}

#[post("/containers/vet-service-db/restart")]
pub async fn restart_vet_service_db_container(
    user: AuthenticatedUser,
    docker: &State<DynDockerPort>,
) -> AppResult<()> {
    ensure_restart_permissions(&user, Option::from(VET_SERVICE_DEV_ROLE))?;

    docker.restart_container("mongo-vet").await
}
