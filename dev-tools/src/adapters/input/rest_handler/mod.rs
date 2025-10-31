mod auth;
mod buckets;
pub mod error;
mod files;
mod users;

mod auth_guard;
mod contracts;
mod docker;
mod mappers;

pub fn routes() -> Vec<rocket::Route> {
    routes![
        files::add_file,
        files::read_files,
        buckets::read_buckets,
        auth::login,
        auth::logout,
        users::add_user,
        docker::auth_service_logs,
    ]
}
