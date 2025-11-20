use crate::adapters::output::mysql_repo::model::user::User;
use crate::application::services::auth::projections::AuthProjection;
use crate::domain::entities::user::UserEntity;
use crate::shared::error::AppError;
use uuid::Uuid;

impl From<User> for UserEntity {
    fn from(user: User) -> Self {
        Self {
            user_id: Uuid::from(user.id),
            email: user.email,
            display_name: user.display_name,
            is_active: user.is_active,
            roles: Default::default(),
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
            roles: Default::default(),
        };
        Ok(AuthProjection { user, pass_hash })
    }
}
