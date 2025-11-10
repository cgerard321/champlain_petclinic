mod querry_execution_routes;
mod schemas;

use crate::adapters::input::http::graphql_handler::querry_execution_routes::graphql_request;
use crate::adapters::input::http::graphql_handler::schemas::sql_schema::QueryRoot;
use async_graphql::{EmptyMutation, EmptySubscription, Schema, http::GraphiQLSource};

pub fn schemas() -> Schema<QueryRoot, EmptyMutation, EmptySubscription> {
    Schema::build(QueryRoot, EmptyMutation, EmptySubscription).finish()
}

pub fn routes_graphql() -> Vec<rocket::Route> {
    routes![graphql_request]
}
