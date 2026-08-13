use chrono::NaiveDateTime;
use uuid::Uuid;

#[derive(Debug)]
pub struct SessionEntity {
    pub id: Uuid,
    pub user_id: Uuid,
    pub created_at: NaiveDateTime,
    pub expires_at: NaiveDateTime,
}

impl SessionEntity {
    pub fn is_expired(&self) -> bool {
        self.expires_at < chrono::Utc::now().naive_utc()
    }
}
