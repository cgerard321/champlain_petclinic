use crate::adapters::input::rest_handler::auth_guard::AuthenticatedUser;
use crate::adapters::input::rest_handler::docker_routes::utils::{
    ensure_logs_permissions, ensure_restart_permissions, ws_logs_for_container,
};
use crate::application::ports::input::docker_logs_port::DynDockerPort;
use crate::core::config::CART_SERVICE_DEV_ROLE;
use crate::core::error::AppResult;
use rocket::State;
use rocket_ws::{Channel, WebSocket};

#[get("/containers/cart-service/logs?<number_of_lines>")]
pub fn cart_service_logs(
    user: AuthenticatedUser,
    ws: WebSocket,
    docker: &State<DynDockerPort>,
    number_of_lines: Option<usize>,
) -> AppResult<Channel<'static>> {
    ensure_logs_permissions(&user, Option::from(CART_SERVICE_DEV_ROLE))?;

    ws_logs_for_container(ws, docker, "cart-service", number_of_lines)
}

#[get("/containers/cart-service-db/logs?<number_of_lines>")]
pub fn cart_service_db_logs(
    user: AuthenticatedUser,
    ws: WebSocket,
    docker: &State<DynDockerPort>,
    number_of_lines: Option<usize>,
) -> AppResult<Channel<'static>> {
    ensure_logs_permissions(&user, Option::from(CART_SERVICE_DEV_ROLE))?;

    ws_logs_for_container(ws, docker, "mongo-carts", number_of_lines)
}

#[post("/containers/cart-service/restart")]
pub async fn restart_cart_service_container(
    user: AuthenticatedUser,
    docker: &State<DynDockerPort>,
) -> AppResult<()> {
    ensure_restart_permissions(&user, Option::from(CART_SERVICE_DEV_ROLE))?;

    docker.restart_container("cart-service").await
}

#[post("/containers/cart-service-db/restart")]
pub async fn restart_cart_service_db_container(
    user: AuthenticatedUser,
    docker: &State<DynDockerPort>,
) -> AppResult<()> {
    ensure_restart_permissions(&user, Option::from(CART_SERVICE_DEV_ROLE))?;

    docker.restart_container("mongo-carts").await
}
