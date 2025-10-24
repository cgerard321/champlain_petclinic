mod business_layer;
mod data_layer;
mod domain_client_layer;
mod handlers;
mod presentation_layer;
mod utils;

#[macro_use]
extern crate rocket;

use crate::presentation_layer::file_service_controller::{add_file, read_buckets, read_files};

#[launch]
fn rocket() -> _ {
    rocket::build()
        .mount("/api/v1", routes![read_files, read_buckets, add_file])
        .register("/", handlers::global_exception_handler::register())
}
