use chrono::NaiveDateTime;
use sqlx::FromRow;
use uuid::fmt::Hyphenated;

#[derive(Debug, FromRow)]
pub struct Session {
    pub id: Hyphenated,
    pub user_id: Hyphenated,
    pub created_at: NaiveDateTime,
    pub expires_at: NaiveDateTime,
}
