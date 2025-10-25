#[macro_use]
extern crate rocket;
mod http;
mod minio;

use crate::db::database::stage;
use crate::http::routes;

mod auth;
mod core;
mod db;
mod users;
mod utils;

use crate::http::prelude::register_catchers;
use crate::minio::store::MinioStore;

#[launch]
fn rocket() -> _ {
    let store = MinioStore::from_env()
        .map_err(|e| {
            eprintln!("Fatal MinIO init error: {e}");
            e
        })
        .expect("MinIO config must be valid at startup");

    rocket::build()
        .attach(stage())
        .manage(store)
        .mount("/api/v1", routes())
        .register("/", register_catchers())
}
