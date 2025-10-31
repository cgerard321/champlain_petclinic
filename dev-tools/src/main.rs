#[macro_use]
extern crate rocket;

use crate::adapters::input::rest_handler::error::register_catchers;
use crate::adapters::input::rest_handler::routes;
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
        .register("/", register_catchers())
}
