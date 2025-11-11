use std::sync::Arc;
use async_graphql::{Context, Object, OutputType};
use crate::application::services::user_context::UserContext;
use crate::shared::error::AppResult;

pub struct QueryRoot;

#[Object]
impl QueryRoot {
    async fn execute_sql_query(
        &self,
        ctx: &Context<'_>,
        service: String,
        sql: String,
    ) -> AppResult<String> {
        let user_ctx = ctx.data::<UserContext>()?;
        let sql_console = ctx.data::<Arc<dyn SqlConsolePort>>()?;
        let result = sql_console.exec_sql_on_service(user_ctx, service, sql).await?;
        Ok(format!("{:?}", result))
    }
}
