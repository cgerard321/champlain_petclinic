use sqlx::FromRow;
use uuid::fmt::Hyphenated;
use veil::Redact;

#[allow(dead_code)]
#[derive(Redact, FromRow)]
pub struct User {
    pub id: Hyphenated,
    pub email: String,
    pub display_name: String,
    pub is_active: bool,
    #[redact(fixed = 3)]
    pub pass_hash: Vec<u8>,
}
