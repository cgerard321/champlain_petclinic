pub struct File {
    pub name: String,
    pub size: u64,
    pub last_modified: Option<String>,
    pub etag: Option<String>,
    pub is_latest: bool,
    pub version_id: Option<String>,
}
