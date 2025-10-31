use crate::adapters::input::rest_handler::auth_guard::AuthenticatedUser;
use crate::adapters::input::rest_handler::docker_routes::utils::{
    ensure_logs_permissions, ensure_restart_permissions, ws_logs_for_container,
};
use crate::application::ports::input::docker_logs_port::DynDockerPort;
use crate::core::config::PRODUCTS_SERVICE_DEV_ROLE;
use crate::core::error::AppResult;
use rocket::State;
use rocket_ws::{Channel, WebSocket};

#[get("/containers/products-service/logs?<number_of_lines>")]
pub fn products_service_logs(
    user: AuthenticatedUser,
    ws: WebSocket,
    docker: &State<DynDockerPort>,
    number_of_lines: Option<usize>,
) -> AppResult<Channel<'static>> {
    ensure_logs_permissions(&user, Option::from(PRODUCTS_SERVICE_DEV_ROLE))?;

    ws_logs_for_container(ws, docker, "products-service", number_of_lines)
}

#[get("/containers/products-service-db/logs?<number_of_lines>")]
pub fn products_service_db_logs(
    user: AuthenticatedUser,
    ws: WebSocket,
    docker: &State<DynDockerPort>,
    number_of_lines: Option<usize>,
) -> AppResult<Channel<'static>> {
    ensure_logs_permissions(&user, Option::from(PRODUCTS_SERVICE_DEV_ROLE))?;

    ws_logs_for_container(ws, docker, "mongo-visit", number_of_lines)
}

#[post("/containers/products-service/restart")]
pub async fn restart_products_service_container(
    user: AuthenticatedUser,
    docker: &State<DynDockerPort>,
) -> AppResult<()> {
    ensure_restart_permissions(&user, Option::from(PRODUCTS_SERVICE_DEV_ROLE))?;

    docker.restart_container("products-service").await
}

#[post("/containers/products-service-db/restart")]
pub async fn restart_products_service_db_container(
    user: AuthenticatedUser,
    docker: &State<DynDockerPort>,
) -> AppResult<()> {
    ensure_restart_permissions(&user, Option::from(PRODUCTS_SERVICE_DEV_ROLE))?;

    docker.restart_container("mongo-products").await
}
