use chrono::NaiveDateTime;
use uuid::Uuid;

#[allow(dead_code)]
#[derive(Debug)]
pub struct Session {
    pub id: Uuid,
    pub user_id: Uuid,
    pub created_at: NaiveDateTime,
    pub expires_at: NaiveDateTime,
}
