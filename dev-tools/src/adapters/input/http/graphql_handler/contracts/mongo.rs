use async_graphql::{Json, SimpleObject};
use serde_json::Value;

#[derive(Debug, SimpleObject, Clone)]
pub struct MongoResultResponseContract {
    pub documents: Vec<Json<Value>>,
    pub count: i64,
}

