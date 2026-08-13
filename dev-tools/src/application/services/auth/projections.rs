use crate::domain::entities::user::UserEntity;
use veil::Redact;

#[derive(Redact)]
pub struct AuthProjection {
    pub user: UserEntity,
    #[redact(fixed = 3)]
    pub pass_hash: Vec<u8>,
}
