use crate::auth::session::Session;
use crate::db::database::Db;
use chrono::NaiveDateTime;
use sqlx::Row;
use uuid::Uuid;

pub async fn insert_session(
    db: &Db,
    session_id: Uuid,
    user_id: Uuid,
    expires_at: NaiveDateTime,
) -> sqlx::Result<()> {
    sqlx::query(
        "INSERT INTO sessions (id, user_id, expires_at)
         VALUES (?, ?, ?)",
    )
        .bind(session_id.to_string())
        .bind(user_id.to_string())
        .bind(expires_at)
        .execute(&db.0)
        .await?;
    Ok(())
}

pub async fn find_session_by_id(db: &Db, sid: Uuid) -> sqlx::Result<Session> {
    let r = sqlx::query(
        "SELECT id, user_id, created_at, expires_at
         FROM sessions WHERE id = ?",
    )
        .bind(sid.to_string())
        .fetch_one(&db.0)
        .await?;

    let id_str: String = r.try_get("id")?;
    let user_id_str: String = r.try_get("user_id")?;

    let id = Uuid::parse_str(&id_str)
        .map_err(|e| sqlx::Error::Protocol(format!("invalid UUID in sessions.id: {e}")))?;
    let user_id = Uuid::parse_str(&user_id_str)
        .map_err(|e| sqlx::Error::Protocol(format!("invalid UUID in sessions.user_id: {e}")))?;

    Ok(Session {
        id,
        user_id,
        created_at: r.try_get("created_at")?,
        expires_at: r.try_get("expires_at")?,
    })
}

pub async fn delete_session(db: &Db, sid: Uuid) -> sqlx::Result<()> {
    sqlx::query("DELETE FROM sessions WHERE id = ?")
        .bind(sid.to_string())
        .execute(&db.0)
        .await?;
    Ok(())
}

#[allow(dead_code)]
// TODO : Add a cron(or similar still unsure) job to delete expired sessions
pub async fn delete_expired_sessions(db: &Db) -> sqlx::Result<u64> {
    let res = sqlx::query("DELETE FROM sessions WHERE expires_at < NOW()")
        .execute(&db.0)
        .await?;
    Ok(res.rows_affected())
}
