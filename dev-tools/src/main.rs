#[macro_use]
extern crate rocket;
mod file_service;
mod http;
mod shared;

use crate::file_service::api::{add_file, read_buckets, read_files};
use crate::file_service::store::MinioStore;
use crate::http::prelude::register_catchers;

#[launch]
fn rocket() -> _ {
    let store = MinioStore::from_env()
        .map_err(|e| {
            eprintln!("Fatal MinIO init error: {e}");
            e
        })
        .expect("MinIO config must be valid at startup");

    rocket::build()
        .manage(store)
        .mount("/api/v1", routes![read_files, read_buckets, add_file])
        .register("/", register_catchers())
}
