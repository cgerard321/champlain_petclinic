mod contracts;
mod error;
mod mappers;
mod query_execution_routes;
mod schemas;

use crate::adapters::input::http::graphql::query_execution_routes::graphql_request;
use crate::adapters::input::http::graphql::schemas::db_console_schema::{MutationRoot, QueryRoot};
use async_graphql::{EmptySubscription, Schema};

pub fn schemas() -> Schema<QueryRoot, MutationRoot, EmptySubscription> {
    Schema::build(QueryRoot, MutationRoot, EmptySubscription).finish()
}

pub fn routes_graphql() -> Vec<rocket::Route> {
    routes![graphql_request]
}
