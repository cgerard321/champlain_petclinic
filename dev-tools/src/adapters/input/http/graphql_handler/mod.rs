mod query_execution_routes;
mod schemas;
mod error;

use crate::adapters::input::http::graphql_handler::query_execution_routes::graphql_request;
use crate::adapters::input::http::graphql_handler::schemas::sql_schema::QueryRoot;
use async_graphql::{EmptyMutation, EmptySubscription, Schema};

pub fn schemas() -> Schema<QueryRoot, EmptyMutation, EmptySubscription> {
    Schema::build(QueryRoot, EmptyMutation, EmptySubscription).finish()
}

pub fn routes_graphql() -> Vec<rocket::Route> {
    routes![graphql_request]
}
