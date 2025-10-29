use crate::adapters::input::rest_handler::contracts::file_contracts::file::FileResponseContract;
use crate::domain::entities::file::FileEntity;

impl From<FileEntity> for FileResponseContract {
    fn from(value: FileEntity) -> Self {
        Self {
            name: value.name,
            size: value.size,
            etag: value.etag,
            version_id: value.version_id,
        }
    }
}
