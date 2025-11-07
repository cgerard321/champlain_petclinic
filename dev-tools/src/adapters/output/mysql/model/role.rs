use sqlx::FromRow;
use uuid::fmt::Hyphenated;

#[derive(Debug, FromRow)]
pub struct Role {
    pub id: Hyphenated,
    pub code: String,
    pub description: Option<String>,
}
