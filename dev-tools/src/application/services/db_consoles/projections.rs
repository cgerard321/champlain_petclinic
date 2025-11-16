use serde_json::Value;

#[derive(Debug)]
pub struct SqlResult {
    pub columns: Vec<String>,
    pub rows: Vec<Vec<String>>,
    pub affected_rows: i64,
}

#[derive(Debug)]
pub struct MongoResult {
    pub bson: Vec<Value>,
    pub affected_count: i64,
}