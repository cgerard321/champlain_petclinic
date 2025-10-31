use crate::adapters::input::rest_handler::docker_routes::auth_service_docker;

mod auth;
mod buckets;
pub mod error;
mod files;
mod users;

mod auth_guard;
mod contracts;
mod docker_routes;
mod mappers;

pub fn routes() -> Vec<rocket::Route> {
    routes![
        files::add_file,
        files::read_files,
        buckets::read_buckets,
        auth::login,
        auth::logout,
        users::add_user,
        auth_service_docker::auth_service_logs,
        auth_service_docker::auth_service_db_logs,
        auth_service_docker::restart_auth_service_container,
        auth_service_docker::restart_auth_service_db_container,
    ]
}
