use crate::adapters::input::rest_handler::dtos::file::file_dto::FileDto;
use crate::domain::entities::file::FileEntity;

impl From<FileEntity> for FileDto {
    fn from(value: FileEntity) -> Self {
        Self {
            name: value.name,
            size: value.size,
            etag: value.etag,
            version_id: value.version_id,
        }
    }
}
