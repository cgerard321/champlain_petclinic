use crate::application::services::db_consoles::projections::SqlResult;
use crate::shared::error::AppResult;

#[async_trait::async_trait]
pub trait SqlDriverPort: Send + Sync {
    async fn execute_query(&self, query: &str) -> AppResult<SqlResult>;
}

pub type DynSqlDriver = dyn SqlDriverPort;
