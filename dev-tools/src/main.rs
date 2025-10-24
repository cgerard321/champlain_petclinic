#[macro_use]
extern crate rocket;
mod file_service;
mod http;

use crate::http::routes;

mod core;

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
        .mount("/api/v1", routes())
        .register("/", register_catchers())
}
