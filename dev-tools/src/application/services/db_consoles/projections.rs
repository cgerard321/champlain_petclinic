use serde_json::Value;

#[derive(Debug)]
pub struct SqlResult {
    pub columns: Vec<String>,
    pub rows: Vec<Vec<String>>,
    pub affected_rows: i64,
}

#[derive(Debug)]
pub struct MongoResult {
    pub collection: String,
    pub documents: Vec<Value>,
    pub count: i64,
}