use crate::adapters::input::http::graphql_handler::schemas::sql_schema::QueryRoot;
use crate::adapters::input::http::guards::auth_guard::AuthenticatedUser;
use crate::application::ports::input::files_port::DynFilesPort;
use crate::application::ports::input::mongo_console_port::DynMongoConsolePort;
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
    sql_port: &State<DynSqlConsolePort>,
    mongo_port: &State<DynMongoConsolePort>,
) -> GraphQLResponse {
    let user_ctx: UserContext = user.into();
    request
        .data(sql_port.inner().clone())
        .data(mongo_port.inner().clone())
        .data(user_ctx)
        .execute(schema.inner())
        .await
}
