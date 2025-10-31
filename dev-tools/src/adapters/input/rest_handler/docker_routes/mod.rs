mod auth_service_docker;
mod utils;
mod vet_service_docker;

pub fn docker_routes() -> Vec<rocket::Route> {
    rocket::routes![
        auth_service_docker::auth_service_logs,
        auth_service_docker::auth_service_db_logs,
        auth_service_docker::restart_auth_service_container,
        auth_service_docker::restart_auth_service_db_container,
        vet_service_docker::vet_service_logs,
        vet_service_docker::vet_service_db_logs,
        vet_service_docker::restart_vet_service_container,
        vet_service_docker::restart_vet_service_db_container,
    ]
}
