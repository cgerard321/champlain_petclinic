use crate::adapters::input::http::graphql_handler::schemas::sql_schema::QueryRoot;
use crate::adapters::input::http::guards::auth_guard::AuthenticatedUser;
use async_graphql::{EmptyMutation, EmptySubscription, Schema};
use async_graphql_rocket::{GraphQLRequest, GraphQLResponse};
use rocket::State;
use crate::application::services::user_context::UserContext;

pub type ExecuteSqlQuerySchema = Schema<QueryRoot, EmptyMutation, EmptySubscription>;

#[post("/", data = "<request>", format = "application/json")]
pub async fn graphql_request(
    schema: &State<ExecuteSqlQuerySchema>,
    request: GraphQLRequest,
    user: AuthenticatedUser,
) -> GraphQLResponse {
    let user_ctx: UserContext = user.into();
    request.data(user_ctx).execute(schema.inner()).await
}
