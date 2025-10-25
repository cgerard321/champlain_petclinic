use crate::db::database::Db;
use crate::users::user::FullUser;
use sqlx::Row;
use uuid::Uuid;

pub async fn insert_user_hashed(
    db: &Db,
    id: Uuid,
    email: &str,
    pass_hash: &[u8],
    display_name: &str,
) -> sqlx::Result<()> {
    sqlx::query(
        "INSERT INTO users (id, email, pass_hash, display_name)
         VALUES ((?), ?, ?, ?)",
    )
    .bind(id.to_string())
    .bind(email)
    .bind(pass_hash)
    .bind(display_name)
    .execute(&db.0)
    .await?;
    Ok(())
}

pub async fn get_user_auth_by_email(db: &Db, email: &str) -> sqlx::Result<FullUser> {
    let row = sqlx::query(
        "SELECT id, email, display_name, is_active, pass_hash
         FROM users WHERE email = ?",
    )
    .bind(email)
    .fetch_one(&db.0)
    .await?;

    Ok(FullUser {
        id: Uuid::parse_str(row.get::<String, _>(0).as_str()).unwrap(),
        email: row.get(1),
        display_name: row.get(2),
        is_active: row.get(3),
        pass_hash: row.get(4),
    })
}
