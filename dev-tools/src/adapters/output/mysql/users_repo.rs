use sqlx::{MySql, Pool, Row};
use std::sync::Arc;
use uuid::Uuid;

use crate::adapters::output::mysql::error::map_sqlx_err;
use crate::application::ports::output::user_repo_port::UsersRepoPort;
use crate::core::error::{AppError, AppResult};
use crate::domain::models::user::FullUser;

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
    ) -> AppResult<()> {
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
            .map_err(|e| map_sqlx_err(e, "User insert"))?;

        Ok(())
    }

    async fn get_user_auth_by_email_full(&self, email: &str) -> AppResult<FullUser> {
        let row = sqlx::query(
            r#"
            SELECT id, email, display_name, is_active, pass_hash
            FROM users
            WHERE email = ?
            "#,
        )
            .bind(email)
            .fetch_one(&*self.pool)
            .await
            .map_err(|e| map_sqlx_err(e, "User lookup"))?;

        let id_str: String = row.try_get("id").map_err(|_| AppError::Internal)?;
        let id = Uuid::parse_str(&id_str).map_err(|_| AppError::Internal)?;

        let email: String = row.try_get("email").map_err(|_| AppError::Internal)?;
        let display_name: String = row
            .try_get("display_name")
            .map_err(|_| AppError::Internal)?;
        let is_active: bool = row.try_get("is_active").map_err(|_| AppError::Internal)?;
        let pass_hash: Vec<u8> = row.try_get("pass_hash").map_err(|_| AppError::Internal)?;

        Ok(FullUser {
            id,
            email,
            display_name,
            is_active,
            pass_hash,
        })
    }
}
