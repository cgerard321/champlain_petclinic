use crate::adapters::input::http::graphql::contracts::mongo::MongoResultResponseContract;
use crate::application::services::db_consoles::projections::MongoResult;
use async_graphql::Json;

impl From<MongoResult> for MongoResultResponseContract {
    fn from(src: MongoResult) -> Self {
        MongoResultResponseContract {
            bson: src.bson.into_iter().map(Json).collect(),
            affected_count: src.affected_count,
        }
    }
}
