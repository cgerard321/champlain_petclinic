use serde::Serialize;

#[derive(Serialize)]
pub(crate) struct FileInfo {
    pub name: String,
    pub size: u64,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub last_modified: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub etag: Option<String>,
    pub is_latest: bool,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub version_id: Option<String>,
}
