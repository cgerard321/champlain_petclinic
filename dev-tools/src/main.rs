#[macro_use]
extern crate rocket;
mod http;
mod minio_service;

use crate::http::routes;

mod core;

use crate::http::prelude::register_catchers;
use crate::minio_service::store::MinioStore;

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
