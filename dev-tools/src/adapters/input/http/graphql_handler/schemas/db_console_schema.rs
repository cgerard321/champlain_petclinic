use crate::adapters::input::http::graphql_handler::contracts::mongo::MongoResultResponseContract;
use crate::adapters::input::http::graphql_handler::contracts::sql::SqlResultResponseContract;
use crate::application::ports::input::mongo_console_port::MongoConsolePort;
use crate::application::ports::input::sql_console_port::SqlConsolePort;
use crate::application::services::user_context::UserContext;
use async_graphql::{Context, Object, Result};
use std::sync::Arc;

pub struct QueryRoot;

#[Object]
impl QueryRoot {
    async fn api_health(&self) -> &str {
        "ok"
    }
}

pub struct MutationRoot;

#[Object]
impl MutationRoot {
    async fn execute_sql_query(
        &self,
        ctx: &Context<'_>,
        service: String,
        sql: String,
    ) -> Result<SqlResultResponseContract> {
        let user_ctx = ctx.data::<UserContext>()?;
        let sql_console = ctx.data::<Arc<dyn SqlConsolePort>>()?;
        let result = sql_console
            .exec_sql_on_service(user_ctx, service, sql)
            .await?;

        Ok(SqlResultResponseContract::from(result))
    }

    async fn execute_mongo_query(
        &self,
        ctx: &Context<'_>,
        service: String,
        mongo_query: String,
    ) -> Result<MongoResultResponseContract> {
        let user_ctx = ctx.data::<UserContext>()?;
        let mongo_console = ctx.data::<Arc<dyn MongoConsolePort>>()?;
        let result = mongo_console
            .exec_mongo_on_service(user_ctx, service, mongo_query)
            .await?;

        Ok(MongoResultResponseContract::from(result))
    }
}
