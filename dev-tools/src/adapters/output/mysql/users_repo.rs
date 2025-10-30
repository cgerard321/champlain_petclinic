use sqlx::{MySql, Pool};
use std::sync::Arc;
use uuid::fmt::Hyphenated;
use uuid::Uuid;

use crate::adapters::output::mysql::error::map_sqlx_err;
use crate::adapters::output::mysql::model::user::User;
use crate::application::ports::output::user_repo_port::UsersRepoPort;
use crate::application::services::auth::projections::AuthProjection;
use crate::core::error::AppResult;
use crate::domain::entities::user::UserEntity;

pub struct MySqlUsersRepo {
    pool: Arc<Pool<MySql>>,
}

impl MySqlUsersRepo {
    pub fn new(pool: Arc<Pool<MySql>>) -> Self {
        Self { pool }
    }
}

#[async_trait::async_trait]
impl UsersRepoPort for MySqlUsersRepo {
    async fn insert_user_hashed(
        &self,
        id: Uuid,
        email: &str,
        pass_hash: &[u8],
        display_name: &str,
    ) -> AppResult<UserEntity> {
        log::info!("Inserting user: {:?}", id);
        let id = Hyphenated::from_uuid(id);

        sqlx::query(
            r#"
            INSERT INTO users (id, email, pass_hash, display_name)
            VALUES (?, ?, ?, ?)
            "#,
        )
        .bind(id)
        .bind(email)
        .bind(pass_hash)
        .bind(display_name)
        .execute(&*self.pool)
        .await
        .map_err(|e| map_sqlx_err(e, "User"))?;

        log::info!("User inserted");

        let user = self.get_user_by_id(id.into_uuid()).await?;

        log::info!("User found: {:?}", user);

        Ok(user)
    }

    async fn get_user_by_id(&self, id: Uuid) -> AppResult<UserEntity> {
        log::info!("Finding user: {:?}", id);
        let id = Hyphenated::from_uuid(id);

        let row: User = sqlx::query_as::<_, User>(
            r#"
        SELECT id, email, display_name, is_active, pass_hash
        FROM users
        WHERE id = ?
        "#,
        )
        .bind(id)
        .fetch_one(&*self.pool)
        .await
        .map_err(|e| map_sqlx_err(e, "User"))?;

        log::info!("User found: {:?}", row);

        Ok(UserEntity::from(row))
    }

    async fn get_user_auth_by_email_for_login(&self, email: &str) -> AppResult<AuthProjection> {
        log::info!("Finding user for login: {:}", email);
        let row: User = sqlx::query_as::<_, User>(
            r#"
        SELECT id, email, display_name, is_active, pass_hash
        FROM users
        WHERE email = ?
        "#,
        )
        .bind(email)
        .fetch_one(&*self.pool)
        .await
        .map_err(|e| map_sqlx_err(e, "User"))?;

        log::info!("User found: {:?}", row);

        Ok(AuthProjection::try_from(row)?)
    }
}
