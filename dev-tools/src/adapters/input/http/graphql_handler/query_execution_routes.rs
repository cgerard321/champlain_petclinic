use crate::adapters::input::http::graphql_handler::schemas::sql_schema::QueryRoot;
use crate::adapters::input::http::guards::auth_guard::AuthenticatedUser;
use crate::application::ports::input::files_port::DynFilesPort;
use crate::application::ports::input::sql_console_port::DynSqlConsolePort;
use crate::application::ports::output::db_drivers::sql_driver::DynSqlDriver;
use crate::application::services::user_context::UserContext;
use async_graphql::{EmptyMutation, EmptySubscription, Schema};
use async_graphql_rocket::{GraphQLRequest, GraphQLResponse};
use rocket::State;

pub type ExecuteSqlQuerySchema = Schema<QueryRoot, EmptyMutation, EmptySubscription>;

#[post("/", data = "<request>", format = "application/json")]
pub async fn graphql_request(
    schema: &State<ExecuteSqlQuerySchema>,
    request: GraphQLRequest,
    user: AuthenticatedUser,
    port: &State<DynSqlConsolePort>,
) -> GraphQLResponse {
    let user_ctx: UserContext = user.into();
    let sql_console_port = port.inner().clone();
    request
        .data(sql_console_port)
        .data(user_ctx)
        .execute(schema.inner())
        .await
}
