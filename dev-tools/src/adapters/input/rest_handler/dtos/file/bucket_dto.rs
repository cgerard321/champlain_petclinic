use serde::Serialize;

#[derive(Debug, Serialize)]
pub struct BucketDto {
    pub name: String,
    pub creation_date: Option<String>,
}
