use sqlx::{MySql, Pool};
use std::sync::Arc;
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
        sqlx::query(
            r#"
            INSERT INTO users (id, email, pass_hash, display_name)
            VALUES (?, ?, ?, ?)
            "#,
        )
        .bind(id.to_string())
        .bind(email)
        .bind(pass_hash)
        .bind(display_name)
        .execute(&*self.pool)
        .await
        .map_err(|e| map_sqlx_err(e, "User"))?;

        let user = self.get_user_by_id(id).await?;

        Ok(user)
    }

    async fn get_user_by_id(&self, id: Uuid) -> AppResult<UserEntity> {
        let row: User = sqlx::query_as::<_, User>(
            r#"
        SELECT id, email, display_name, is_active
        FROM users
        WHERE id = ?
        "#,
        )
        .bind(id.to_string())
        .fetch_one(&*self.pool)
        .await
        .map_err(|e| map_sqlx_err(e, "User"))?;

        Ok(UserEntity {
            user_id: row.id,
            email: row.email,
            display_name: row.display_name,
            is_active: row.is_active,
        })
    }

    async fn get_user_auth_by_email_for_login(&self, email: &str) -> AppResult<AuthProjection> {
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

        Ok(AuthProjection {
            user: UserEntity {
                user_id: row.id,
                email: row.email,
                display_name: row.display_name,
                is_active: row.is_active,
            },
            pass_hash: row.pass_hash,
        })
    }
}
