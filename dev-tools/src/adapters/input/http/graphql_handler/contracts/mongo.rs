use async_graphql::{SimpleObject, Value};

#[derive(Debug, SimpleObject, Clone)]
pub struct MongoResultResponseContract {
    pub collection: String,
    pub documents: Vec<Value>,
    pub count: i64,
}

