use crate::adapters::output::mysql::error::map_sqlx_err;
use crate::adapters::output::mysql::model::session::Session;
use crate::application::ports::output::auth_repo_port::AuthRepoPort;
use crate::core::error::{AppError, AppResult};
use crate::domain::entities::session::SessionEntity;
use chrono::NaiveDateTime;
use sqlx::{MySql, Pool};
use std::sync::Arc;
use uuid::fmt::Hyphenated;
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
    async fn insert_session(
        &self,
        sid: Uuid,
        uid: Uuid,
        exp: NaiveDateTime,
    ) -> AppResult<SessionEntity> {
        log::info!("Inserting session: {:?}", sid);
        // I tested and it does not seem to be needed, but for the sake of uniformity I will
        // convert it, and it will also make sure the format is correct.
        let sid = Hyphenated::from_uuid(sid);
        let uid = Hyphenated::from_uuid(uid);

        sqlx::query("INSERT INTO sessions (id, user_id, expires_at) VALUES (?, ?, ?)")
            .bind(sid.to_string())
            .bind(uid.to_string())
            .bind(exp)
            .execute(&*self.pool)
            .await
            .map_err(|e| map_sqlx_err(e, "Sessions"))?;

        log::info!("Session inserted");

        let session = self.find_session_by_id(Uuid::from(sid)).await?;

        log::info!("Session found: {:?}", session);

        Ok(session)
    }

    async fn find_session_by_id(&self, sid: Uuid) -> AppResult<SessionEntity> {
        log::info!("Finding session: {:?}", sid);
        let sid = Hyphenated::from_uuid(sid);
        let row: Option<Session> = sqlx::query_as::<_, Session>(
            "SELECT id, user_id, created_at, expires_at FROM sessions WHERE id = ?",
        )
        .bind(sid)
        .fetch_optional(&*self.pool)
        .await
        .map_err(|e| map_sqlx_err(e, "Sessions"))?;

        let Some(row) = row else {
            return Err(AppError::Unauthorized);
        };

        log::info!("Session found: {:?}", row);

        Ok(SessionEntity::from(row))
    }

    async fn delete_session(&self, sid: Uuid) -> AppResult<()> {
        log::info!("Deleting session: {:?}", sid);
        let sid = Hyphenated::from_uuid(sid);
        sqlx::query("DELETE FROM sessions WHERE id = ?")
            .bind(sid.to_string())
            .execute(&*self.pool)
            .await
            .map_err(|e| map_sqlx_err(e, "Sessions"))?;

        log::info!("Session deleted");
        Ok(())
    }

    #[allow(dead_code)]
    async fn delete_expired_sessions(&self) -> AppResult<u64> {
        let res = sqlx::query("DELETE FROM sessions WHERE expires_at < NOW()")
            .execute(&*self.pool)
            .await
            .map_err(|e| map_sqlx_err(e, "Sessions"))?;
        Ok(res.rows_affected())
    }
}
