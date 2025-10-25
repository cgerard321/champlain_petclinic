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

pub async fn find_session_by_id(db: &Db, sid: Uuid) -> sqlx::Result<Option<Session>> {
    let row = sqlx::query(
        "SELECT id, user_id, created_at, expires_at
         FROM sessions WHERE id = ?",
    )
    .bind(sid.to_string())
    .fetch_optional(&db.0)
    .await?;

    Ok(row.map(|r| Session {
        id: Uuid::parse_str(r.get::<String, _>(0).as_str()).unwrap(),
        user_id: Uuid::parse_str(r.get::<String, _>(1).as_str()).unwrap(),
        created_at: r.get(2),
        expires_at: r.get(3),
    }))
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
