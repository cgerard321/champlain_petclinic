use async_graphql::SimpleObject;

#[derive(Debug, SimpleObject, Clone)]
pub struct SqlResultResponseContract {
    pub columns: Vec<String>,
    pub rows: Vec<Vec<String>>,
    pub affected_rows: i64,
}

