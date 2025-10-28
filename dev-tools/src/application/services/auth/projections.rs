use crate::domain::entities::user::UserEntity;

pub struct AuthProjection {
    pub user: UserEntity,
    pub pass_hash: Vec<u8>,
}
