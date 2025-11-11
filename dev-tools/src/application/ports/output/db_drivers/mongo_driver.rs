use crate::application::services::db_consoles::projections::MongoResult;
use crate::shared::error::AppResult;

#[async_trait::async_trait]
pub trait MongoDriverPort: Send + Sync {
    async fn execute_query(&self, mongo_command: &str, database_name: &str) -> AppResult<MongoResult>;
}

pub type DynMongoDriver = dyn MongoDriverPort;
