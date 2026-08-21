use crate::adapters::output::db::mysql::repo::model::session::Session;
use crate::domain::entities::session::SessionEntity;
use uuid::Uuid;

impl From<Session> for SessionEntity {
    fn from(session: Session) -> Self {
        Self {
            id: Uuid::from(session.id),
            user_id: Uuid::from(session.user_id),
            created_at: session.created_at,
            expires_at: session.expires_at,
        }
    }
}
