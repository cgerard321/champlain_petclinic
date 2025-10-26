#[macro_use]
extern crate rocket;

use crate::adapters::input::error::register_catchers;
use crate::adapters::input::routes;
use crate::adapters::output::minio::store::MinioStore;
use bootstrap::stage;

mod core;

mod adapters;
pub mod bootstrap;
mod domain;

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
