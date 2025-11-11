use crate::adapters::input::http::graphql_handler::contracts::mongo::MongoResultResponseContract;
use crate::application::services::db_consoles::projections::MongoResult;
use async_graphql::Json;

impl From<MongoResult> for MongoResultResponseContract {
    fn from(src: MongoResult) -> Self {
        MongoResultResponseContract {
            collection: src.collection,
            documents: src.documents.into_iter().map(Json).collect(),
            count: src.count,
        }
    }
}
