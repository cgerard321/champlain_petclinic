use crate::db::database::Db;
use chrono::NaiveDateTime;
use sqlx::Row;
use uuid::Uuid;
use crate::auth::session::Session;

pub async fn insert_session(
    db: &Db,
    session_id: Uuid,
    user_id: Uuid,
    expires_at: NaiveDateTime,
    ip: Option<&str>,
) -> sqlx::Result<()> {
    sqlx::query(
        "INSERT INTO sessions (id, user_id, expires_at, ip)
         VALUES (?, ?, ?, ?, ?)",
    )
        .bind(session_id.to_string())
        .bind(user_id.to_string())
        .bind(expires_at)
        .bind(ip)
        .execute(&db.0)
        .await?;
    Ok(())
}

pub async fn find_session_by_id(db: &Db, sid: Uuid) -> sqlx::Result<Option<Session>> {
    let row = sqlx::query(
        "SELECT id, user_id, created_at, expires_at, user_agent, ip
         FROM sessions WHERE id = ?"
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

pub async fn delete_expired_sessions(db: &Db) -> sqlx::Result<u64> {
    let res = sqlx::query("DELETE FROM sessions WHERE expires_at < NOW()")
        .execute(&db.0)
        .await?;
    Ok(res.rows_affected())
}
