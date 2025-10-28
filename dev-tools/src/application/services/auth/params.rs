use uuid::Uuid;

pub struct UserLoginParams {
    pub user_id: Uuid,
    pub email: String,
    pub pass_hash: Vec<u8>,
    pub is_active: bool,
}
