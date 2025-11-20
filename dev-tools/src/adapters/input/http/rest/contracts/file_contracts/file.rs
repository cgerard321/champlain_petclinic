use rocket::serde::Serialize;

#[derive(Serialize, Debug)]
pub struct FileResponseContract {
    pub name: String,
    pub size: u64,
    pub etag: Option<String>,
}
