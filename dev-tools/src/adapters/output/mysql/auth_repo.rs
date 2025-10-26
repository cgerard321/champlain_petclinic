use crate::adapters::output::mysql::error::map_sqlx_err;
use crate::application::ports::output::auth_repo_port::AuthRepoPort;
use crate::core::error::{AppError, AppResult};
use crate::domain::models::session::Session;
use chrono::NaiveDateTime;
use sqlx::{MySql, Pool, Row};
use std::sync::Arc;
use uuid::Uuid;

pub struct MySqlAuthRepo {
    pool: Arc<Pool<MySql>>,
}

impl MySqlAuthRepo {
    pub fn new(pool: Arc<Pool<MySql>>) -> Self {
        Self { pool }
    }
}

#[async_trait::async_trait]
impl AuthRepoPort for MySqlAuthRepo {
    async fn insert_session(&self, sid: Uuid, uid: Uuid, exp: NaiveDateTime) -> AppResult<Session> {
        sqlx::query("INSERT INTO sessions (id, user_id, expires_at) VALUES (?, ?, ?)")
            .bind(sid.to_string())
            .bind(uid.to_string())
            .bind(exp)
            .execute(&*self.pool)
            .await
            .map_err(|e| map_sqlx_err(e, "Sessions"))?;

        Ok(Session {
            id: sid,
            user_id: uid,
            created_at: chrono::Utc::now().naive_utc(),
            expires_at: exp,
        })
    }

    async fn find_session_by_id(&self, sid: Uuid) -> AppResult<Session> {
        let row =
            sqlx::query("SELECT id, user_id, created_at, expires_at FROM sessions WHERE id = ?")
                .bind(sid.to_string())
                .fetch_optional(&*self.pool)
                .await
                .map_err(|e| map_sqlx_err(e, "Sessions"))?;

        let Some(row) = row else {
            return Err(AppError::Unauthorized);
        };

        Ok(Session {
            id: sid,
            user_id: Uuid::parse_str(row.get::<String, _>("user_id").as_str())
                .unwrap_or(Uuid::nil()),
            created_at: row.get("created_at"),
            expires_at: row.get("expires_at"),
        })
    }

    async fn delete_session(&self, sid: Uuid) -> AppResult<()> {
        sqlx::query("DELETE FROM sessions WHERE id = ?")
            .bind(sid.to_string())
            .execute(&*self.pool)
            .await
            .map_err(|e| map_sqlx_err(e, "Sessions"))?;
        Ok(())
    }

    async fn delete_expired_sessions(&self) -> AppResult<u64> {
        let res = sqlx::query("DELETE FROM sessions WHERE expires_at < NOW()")
            .execute(&*self.pool)
            .await
            .map_err(|e| map_sqlx_err(e, "Sessions"))?;
        Ok(res.rows_affected())
    }
}
