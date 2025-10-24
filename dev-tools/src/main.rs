#[macro_use]
extern crate rocket;
mod handlers;
mod file_service_subdomain;

use crate::file_service_subdomain::presentation_layer::file_service_controller::{add_file, read_buckets, read_files};

#[launch]
fn rocket() -> _ {
    rocket::build()
        .mount("/api/v1", routes![read_files, read_buckets, add_file])
        .register("/", handlers::global_exception_handler::register())
}
