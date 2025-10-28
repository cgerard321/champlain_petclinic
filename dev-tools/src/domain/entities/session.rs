use chrono::NaiveDateTime;
use uuid::Uuid;

pub struct SessionEntity {
    pub id: Uuid,
    pub user_id: Uuid,
    pub created_at: NaiveDateTime,
    pub expires_at: NaiveDateTime,
}
