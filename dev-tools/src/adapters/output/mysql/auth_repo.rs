use crate::adapters::output::mysql::error::map_sqlx_err;
use crate::bootstrap::Db;
use crate::core::error::AppResult;
use crate::domain::models::session::Session;
use chrono::NaiveDateTime;
use sqlx::Row;
use uuid::Uuid;

pub async fn insert_session(
    db: &Db,
    session_id: Uuid,
    user_id: Uuid,
    expires_at: NaiveDateTime,
) -> AppResult<()> {
    sqlx::query(
        "INSERT INTO sessions (id, user_id, expires_at)
         VALUES (?, ?, ?)",
    )
    .bind(session_id.to_string())
    .bind(user_id.to_string())
    .bind(expires_at)
    .execute(&db.0)
    .await
    .map_err(|e| map_sqlx_err(e, "Session insert"))?;
    Ok(())
}

pub async fn find_session_by_id(db: &Db, sid: Uuid) -> AppResult<Session> {
    let row = sqlx::query(
        "SELECT id, user_id, created_at, expires_at
         FROM sessions WHERE id = ?",
    )
    .bind(sid.to_string())
    .fetch_one(&db.0)
    .await
    .map_err(|e| map_sqlx_err(e, "Session"))?;

    Ok(Session {
        id: Uuid::parse_str(row.get::<String, _>(0).as_str()).unwrap(),
        user_id: Uuid::parse_str(row.get::<String, _>(1).as_str()).unwrap(),
        created_at: row.get(2),
        expires_at: row.get(3),
    })
}

pub async fn delete_session(db: &Db, sid: Uuid) -> AppResult<()> {
    sqlx::query("DELETE FROM sessions WHERE id = ?")
        .bind(sid.to_string())
        .execute(&db.0)
        .await
        .map_err(|e| map_sqlx_err(e, "Session delete"))?;
    Ok(())
}

pub async fn delete_expired_sessions(db: &Db) -> AppResult<u64> {
    let res = sqlx::query("DELETE FROM sessions WHERE expires_at < NOW()")
        .execute(&db.0)
        .await
        .map_err(|e| map_sqlx_err(e, "Delete expired sessions"))?;
    Ok(res.rows_affected())
}
