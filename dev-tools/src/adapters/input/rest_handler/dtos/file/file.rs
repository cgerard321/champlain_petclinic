use rocket::serde::Serialize;

#[derive(Serialize, Debug)]
pub struct FileDto {
    pub name: String,
    pub size: u64,
    pub etag: Option<String>,
    pub version_id: Option<String>,
}
