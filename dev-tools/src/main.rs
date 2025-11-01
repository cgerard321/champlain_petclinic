#[macro_use]
extern crate rocket;

use crate::adapters::input::rest_handler::error::register_catchers;
use crate::adapters::input::rest_handler::{routes, routes_docker};
use bootstrap::stage;
use rocket::State;

mod core;

mod adapters;
mod application;
pub mod bootstrap;
mod domain;
struct Endpoints(Vec<String>);

#[get("/")]
fn list_endpoints(endpoints: &State<Endpoints>) -> String {
    log::info!("Returning endpoints");
    endpoints.0.join("\n")
}

#[launch]
fn rocket() -> _ {
    env_logger::init();
    let rocket = rocket::build()
        .attach(stage())
        .mount("/endpoints", routes![list_endpoints])
        .mount("/api/v1", routes())
        .mount("/api/v1/services", routes_docker())
        .register("/", register_catchers());

    let endpoints: Vec<String> = rocket
        .routes()
        .map(|r| format!("{} {}", r.method, r.uri))
        .collect();

    // This is very hacky, but since we use WebSockets the Swagger
    // libraries do not play nice with them, so we just list the endpoints
    rocket.manage(Endpoints(endpoints))
}
