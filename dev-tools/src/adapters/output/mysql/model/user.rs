use sqlx::FromRow;
use uuid::Uuid;

#[allow(dead_code)]
#[derive(Debug, FromRow)]
pub struct User {
    pub id: Uuid,
    pub email: String,
    pub display_name: String,
    pub is_active: bool,
    pub pass_hash: Vec<u8>,
}

