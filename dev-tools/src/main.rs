#[macro_use]
extern crate rocket;

use std::sync::Arc;
use crate::adapters::input::rest_handler::error::register_catchers;
use crate::adapters::input::rest_handler::routes;
use bootstrap::stage;
use crate::adapters::output::minio::store::MinioStore;
use crate::application::ports::output::file_storage_port::DynFileStorage;

mod core;

mod adapters;
pub mod bootstrap;
mod domain;
mod application;

#[launch]
fn rocket() -> _ {
    let store = MinioStore::from_env()
        .map_err(|e| {
            eprintln!("Fatal MinIO init error: {e}");
            e
        })
        .expect("MinIO config must be valid at startup");

    let storage: DynFileStorage = Arc::new(
        store
    );

    rocket::build()
        .attach(stage())
        .manage(storage)
        .mount("/api/v1", routes())
        .register("/", register_catchers())
}
