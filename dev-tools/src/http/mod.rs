mod auth;
mod buckets;
pub mod error;
mod files;
pub mod prelude;
mod users;

pub fn routes() -> Vec<rocket::Route> {
    routes![
        files::add_file,
        files::read_files,
        buckets::read_buckets,
        auth::login,
        auth::logout,
        users::add_user,
        users::me,
    ]
}
