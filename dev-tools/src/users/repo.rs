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

    let id_str: String = row.try_get("id")?;
    let id = Uuid::parse_str(&id_str).map_err(|_| sqlx::Error::ColumnDecode {
        index: "id".into(),
        source: Box::new(std::fmt::Error),
    })?;

    Ok(FullUser {
        id,
        email: row.try_get("email")?,
        display_name: row.try_get("display_name")?,
        is_active: row.try_get("is_active")?,
        pass_hash: row.try_get("pass_hash")?,
    })
}
