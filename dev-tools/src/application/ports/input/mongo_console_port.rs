use crate::application::ports::input::files_port::FilesPort;
use crate::application::services::db_consoles::projections::{MongoResult, SqlResult};
use crate::application::services::user_context::UserContext;
use crate::shared::error::AppResult;

#[async_trait::async_trait]
pub trait MongoConsolePort: Send + Sync {
    async fn exec_mongo_on_service(
        &self,
        user_ctx: &UserContext,
        service: String,
        mongo_query: String,
    ) -> AppResult<MongoResult>;
}

pub type DynMongoConsolePort = std::sync::Arc<dyn MongoConsolePort>;

