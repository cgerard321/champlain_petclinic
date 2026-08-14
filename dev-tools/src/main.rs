#[macro_use]
extern crate rocket;
use crate::adapters::input::http::graphql::{routes_graphql, schemas};
use crate::adapters::input::http::rest::error::register_catchers;
use crate::adapters::input::http::rest::{routes, routes_docker};
use bootstrap::stage;
use rocket::State;
use rocket_cors::{AllowedOrigins, CorsOptions};
use tikv_jemallocator::Jemalloc;

// I found this allocator to be more memory efficient than the default one
#[global_allocator] static GLOBAL: Jemalloc = Jemalloc;

mod shared;

mod adapters;
mod application;
pub mod bootstrap;
mod domain;
struct Endpoints(Vec<String>);

#[get("/")]
fn list_endpoints(endpoints: &State<Endpoints>) -> String {
    log::debug!("Returning endpoints");
    endpoints.0.join("\n")
}

#[launch]
fn rocket() -> _ {
    env_logger::init();
    let prod_url = ["https://petclinic-management-ui.benmusicgeek.synology.me"];
    let dev_url = ["http://localhost:4200"];
    let cors = CorsOptions::default()
        .allowed_origins(AllowedOrigins::some(&prod_url, &dev_url))
        .allow_credentials(true)
        .to_cors()
        .expect("Error creating CORS");

    let rocket = rocket::build()
        .attach(cors)
        .attach(stage())
        // REST
        .mount("/endpoints", routes![list_endpoints])
        .mount("/api/v1", routes())
        .mount("/api/v1/services", routes_docker())
        // GraphQL
        .manage(schemas())
        .mount("/api/v1/graphql", routes_graphql())
        .register("/", register_catchers());

    let endpoints: Vec<String> = rocket
        .routes()
        .map(|r| format!("{} {}", r.method, r.uri))
        .collect();

    // This is very hacky, but since we use WebSockets the Swagger
    // libraries do not play nice with them, so we just list the endpoints
    rocket.manage(Endpoints(endpoints))
}
