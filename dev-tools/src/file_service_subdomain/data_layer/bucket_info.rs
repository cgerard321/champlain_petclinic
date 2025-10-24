use rocket::serde::Serialize;

#[derive(Serialize)]
pub(crate) struct BucketInfo {
    pub(crate) name: String,
    pub(crate) creation_date: Option<String>,
}
