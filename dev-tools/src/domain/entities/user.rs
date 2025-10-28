use uuid::Uuid;

pub struct UserEntity {
    pub user_id: Uuid,
    pub email: String,
    pub display_name: String,
    pub is_active: bool,
}

pub struct UserLogin {
}