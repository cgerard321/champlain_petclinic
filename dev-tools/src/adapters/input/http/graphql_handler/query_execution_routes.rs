use crate::adapters::input::http::graphql_handler::schemas::sql_schema::QueryRoot;
use async_graphql::{EmptyMutation, EmptySubscription, Schema};
use async_graphql_rocket::{GraphQLRequest, GraphQLResponse};
use rocket::State;

pub type ExecuteSqlQuerySchema = Schema<QueryRoot, EmptyMutation, EmptySubscription>;

#[post("/", data = "<request>", format = "application/json")]
pub async fn graphql_request(
    schema: &State<ExecuteSqlQuerySchema>,
    request: GraphQLRequest,
) -> GraphQLResponse {
    request.execute(schema.inner()).await
}
