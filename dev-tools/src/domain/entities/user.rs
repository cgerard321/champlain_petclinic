use uuid::Uuid;

#[derive(Debug)]
pub struct UserEntity {
    pub user_id: Uuid,
    pub email: String,
    pub display_name: String,
    pub is_active: bool,
}
