#[derive(Debug)]
pub struct FileEntity {
    pub name: String,
    pub size: u64,
    pub etag: Option<String>,
}
