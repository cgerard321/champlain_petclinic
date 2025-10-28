use uuid::Uuid;
use crate::adapters::output::mysql::model::user::User;
use crate::domain::entities::user::UserEntity;

impl From<User> for UserEntity {
    fn from(user: User) -> Self {
        Self {
            user_id: Uuid::from(user.id),
            email: user.email,
            display_name: user.display_name,
            is_active: user.is_active,
        }
    }
}
