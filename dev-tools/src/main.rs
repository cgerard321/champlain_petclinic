#[macro_use]
extern crate rocket;

use crate::adapters::input::rest_handler::error::register_catchers;
use crate::adapters::input::rest_handler::{routes, routes_docker};
use bootstrap::stage;

mod core;

mod adapters;
mod application;
pub mod bootstrap;
mod domain;

#[launch]
fn rocket() -> _ {
    env_logger::init();
    rocket::build()
        .attach(stage())
        .mount("/api/v1", routes())
        .mount("/api/v1/docker", routes_docker())
        .register("/", register_catchers())
}
