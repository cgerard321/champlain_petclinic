mod docker;

mod utils;

pub fn docker_routes() -> Vec<rocket::Route> {
    rocket::routes![docker::service_logs, docker::restart_service_container,]
}
