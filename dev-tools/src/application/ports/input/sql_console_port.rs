use crate::application::services::db_consoles::projections::SqlResult;
use crate::application::services::user_context::UserContext;
use crate::shared::error::AppResult;

#[async_trait::async_trait]
pub trait SqlConsolePort: Send + Sync {
    async fn exec_sql_on_service(
        &self,
        user_ctx: &UserContext,
        service: String,
        sql: String,
    ) -> AppResult<SqlResult>;
}