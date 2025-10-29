use serde::Serialize;

#[derive(Debug, Serialize)]
pub struct BucketResponseContract {
    pub name: String,
    pub creation_date: Option<String>,
}
