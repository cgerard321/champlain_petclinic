use crate::adapters::input::rest_handler::dtos::file::bucket_dto::BucketDto;
use crate::domain::entities::bucket::BucketEntity;

impl From<BucketEntity> for BucketDto {
    fn from(value: BucketEntity) -> Self {
        Self {
            name: value.name,
            creation_date: value.creation_date,
        }
    }
}
