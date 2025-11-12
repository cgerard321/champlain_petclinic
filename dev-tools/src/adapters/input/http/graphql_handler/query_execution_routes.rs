use crate::adapters::input::http::graphql_handler::schemas::db_console_schema::{MutationRoot, QueryRoot};
use crate::adapters::input::http::guards::auth_guard::AuthenticatedUser;
use crate::application::ports::input::mongo_console_port::DynMongoConsolePort;
use crate::application::ports::input::sql_console_port::DynSqlConsolePort;
use crate::application::services::user_context::UserContext;
use async_graphql::{EmptyMutation, EmptySubscription, Schema};
use async_graphql_rocket::{GraphQLRequest, GraphQLResponse};
use rocket::State;
use crate::application::ports::output::docker_api::DynDockerAPI;

pub type ExecuteDatabaseQuerySchema = Schema<QueryRoot, MutationRoot, EmptySubscription>;

#[post("/", data = "<request>", format = "application/json")]
pub async fn graphql_request(
    schema: &State<ExecuteDatabaseQuerySchema>,
    request: GraphQLRequest,
    user: AuthenticatedUser,
    sql_port: &State<DynSqlConsolePort>,
    mongo_port: &State<DynMongoConsolePort>,
    docker_port: &State<DynDockerAPI>,
) -> GraphQLResponse {
    let user_ctx: UserContext = user.into();
    request
        .data(sql_port.inner().clone())
        .data(mongo_port.inner().clone())
        .data(docker_port.inner().clone())
        .data(user_ctx)
        .execute(schema.inner())
        .await
}
