use crate::adapters::output::mysql::model::user::User;
use crate::application::services::auth::projections::AuthProjection;
use crate::core::error::AppError;
use crate::domain::entities::user::UserEntity;
use uuid::Uuid;

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

impl TryFrom<User> for AuthProjection {
    type Error = AppError;

    fn try_from(row: User) -> Result<Self, Self::Error> {
        let User {
            id,
            email,
            display_name,
            pass_hash,
            ..
        } = row;

        let user = UserEntity {
            user_id: Uuid::from(id),
            email,
            display_name,
            is_active: row.is_active,
        };
        Ok(AuthProjection { user, pass_hash })
    }
}
