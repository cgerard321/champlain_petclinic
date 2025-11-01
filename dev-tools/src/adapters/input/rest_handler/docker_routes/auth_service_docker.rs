use crate::adapters::input::rest_handler::auth_guard::AuthenticatedUser;
use crate::adapters::input::rest_handler::contracts::docker_contracts::docker::ContainerActionRequestContract;
use crate::adapters::input::rest_handler::docker_routes::utils::{
    ensure_logs_permissions, ensure_restart_permissions, ws_logs_for_container,
};
use crate::application::ports::input::docker_logs_port::DynDockerPort;
use crate::core::config::AUTH_SERVICE_DEV_ROLE;
use crate::core::error::{AppError, AppResult};
use rocket::serde::json::Json;
use rocket::State;
use rocket_ws::{Channel, WebSocket};

#[get("/auth/actions/tail?<container_type>&<number_of_lines>")]
pub fn auth_logs(
    user: AuthenticatedUser,
    ws: WebSocket,
    docker: &State<DynDockerPort>,
    container_type: &str,
    number_of_lines: Option<usize>,
) -> AppResult<Channel<'static>> {
    ensure_logs_permissions(&user, Option::from(AUTH_SERVICE_DEV_ROLE))?;

    if container_type.is_empty() {
        return Err(AppError::BadRequest("Container type is empty".to_string()));
    }
    let result = determine_container_type(container_type)?;

    ws_logs_for_container(ws, docker, result, number_of_lines)
}
#[post(
    "/auth/actions/restart",
    format = "application/json",
    data = "<restart_request>"
)]
pub async fn restart_auth_container(
    user: AuthenticatedUser,
    docker: &State<DynDockerPort>,
    restart_request: Json<ContainerActionRequestContract>,
) -> AppResult<()> {
    ensure_restart_permissions(&user, Option::from(AUTH_SERVICE_DEV_ROLE))?;

    let container_type = &restart_request.container_type;

    if container_type.is_empty() {
        return Err(AppError::BadRequest("Container type is empty".to_string()));
    }

    let result: &str = determine_container_type(container_type)?;

    docker.restart_container(result).await
}

fn determine_container_type(container_type: &str) -> AppResult<&'static str> {
    if container_type.eq_ignore_ascii_case("service") {
        Ok("auth-service")
    } else if container_type.eq_ignore_ascii_case("db") {
        Ok("mysql-auth")
    } else {
        return Err(AppError::BadRequest(format!(
            "Invalid container type: {}",
            container_type
        )));
    }
}
