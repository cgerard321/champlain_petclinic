use sqlx::FromRow;
use uuid::fmt::Hyphenated;

#[allow(dead_code)]
#[derive(Debug, FromRow)]
pub struct User {
    pub id: Hyphenated,
    pub email: String,
    pub display_name: String,
    pub is_active: bool,
    pub pass_hash: Vec<u8>,
}
