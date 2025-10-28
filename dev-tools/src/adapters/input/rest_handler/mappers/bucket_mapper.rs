use crate::adapters::input::rest_handler::contracts::file_contracts::bucket::BucketResponseContract;
use crate::domain::entities::bucket::BucketEntity;

impl From<BucketEntity> for BucketResponseContract {
    fn from(value: BucketEntity) -> Self {
        Self {
            name: value.name,
            creation_date: value.creation_date,
        }
    }
}
