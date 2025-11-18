use async_graphql::{Json, SimpleObject};
use serde_json::Value;

#[derive(Debug, SimpleObject, Clone)]
pub struct MongoResultResponseContract {
    pub bson: Vec<Json<Value>>,
    pub affected_count: i64,
}

