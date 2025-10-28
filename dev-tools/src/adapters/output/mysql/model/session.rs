use chrono::NaiveDateTime;
use sqlx::{Decode, FromRow};
use uuid::Uuid;

#[derive(Debug, FromRow)]
pub struct Session {
    pub id: Uuid,
    pub user_id: Uuid,
    pub created_at: NaiveDateTime,
    pub expires_at: NaiveDateTime,
}
