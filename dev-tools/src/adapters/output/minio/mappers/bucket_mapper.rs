use crate::domain::entities::bucket::BucketEntity;
use minio::s3::types::Bucket;

impl From<Bucket> for BucketEntity {
    fn from(b: Bucket) -> Self {
        BucketEntity {
            name: b.name,
            creation_date: Some(b.creation_date.to_string()),
        }
    }
}
