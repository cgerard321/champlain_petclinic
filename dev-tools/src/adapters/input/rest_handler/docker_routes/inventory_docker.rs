use crate::adapters::input::rest_handler::auth_guard::AuthenticatedUser;
use crate::adapters::input::rest_handler::docker_routes::utils::{
    ensure_logs_permissions, ensure_restart_permissions, ws_logs_for_container,
};
use crate::application::ports::input::docker_logs_port::DynDockerPort;
use crate::core::config::INVENTORY_SERVICE_DEV_ROLE;
use crate::core::error::AppResult;
use rocket::State;
use rocket_ws::{Channel, WebSocket};

#[get("/containers/inventory-service/logs?<number_of_lines>")]
pub fn inventory_service_logs(
    user: AuthenticatedUser,
    ws: WebSocket,
    docker: &State<DynDockerPort>,
    number_of_lines: Option<usize>,
) -> AppResult<Channel<'static>> {
    ensure_logs_permissions(&user, Option::from(INVENTORY_SERVICE_DEV_ROLE))?;

    ws_logs_for_container(ws, docker, "inventory-service", number_of_lines)
}

#[get("/containers/inventory-service-db/logs?<number_of_lines>")]
pub fn inventory_service_db_logs(
    user: AuthenticatedUser,
    ws: WebSocket,
    docker: &State<DynDockerPort>,
    number_of_lines: Option<usize>,
) -> AppResult<Channel<'static>> {
    ensure_logs_permissions(&user, Option::from(INVENTORY_SERVICE_DEV_ROLE))?;

    ws_logs_for_container(ws, docker, "mongo-visit", number_of_lines)
}

#[post("/containers/inventory-service/restart")]
pub async fn restart_inventory_service_container(
    user: AuthenticatedUser,
    docker: &State<DynDockerPort>,
) -> AppResult<()> {
    ensure_restart_permissions(&user, Option::from(INVENTORY_SERVICE_DEV_ROLE))?;

    docker.restart_container("inventory-service").await
}

#[post("/containers/inventory-service-db/restart")]
pub async fn restart_inventory_service_db_container(
    user: AuthenticatedUser,
    docker: &State<DynDockerPort>,
) -> AppResult<()> {
    ensure_restart_permissions(&user, Option::from(INVENTORY_SERVICE_DEV_ROLE))?;

    docker.restart_container("mongo-inventory").await
}
