use crate::domain::entities::file::FileEntity;
use minio::s3::response::PutObjectResponse;
use minio::s3::types::ListEntry;

impl From<ListEntry> for FileEntity {
    fn from(o: ListEntry) -> Self {
        FileEntity {
            name: o.name,
            size: o.size.unwrap_or(0),
            etag: o.etag,
            version_id: o.version_id,
        }
    }
}

pub struct PutObjectMap {
    pub resp: PutObjectResponse,
    pub size: u64,
}

impl From<PutObjectMap> for FileEntity {
    fn from(m: PutObjectMap) -> Self {
        FileEntity {
            name: m.resp.object,
            size: m.size,
            etag: Option::from(m.resp.etag),
            version_id: m.resp.version_id,
        }
    }
}
