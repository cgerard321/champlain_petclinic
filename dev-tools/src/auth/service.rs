use crate::auth::repo as session_repo;
use crate::core::config::DEFAULT_SESSIONS_AGE_HR;
use crate::core::error::{AppError, AppResult};
use crate::db::database::Db;
use crate::db::error::map_sqlx_err;
use crate::users::service as user_service;
use crate::utils::auth::get_pepper;
use argon2::{Argon2, PasswordHash, PasswordVerifier};
use chrono::{Duration, NaiveDateTime, Utc};
use rocket::State;
use uuid::Uuid;

fn verify_password(stored_hash_bytes: &[u8], candidate: &str, pepper: &str) -> AppResult<bool> {
    let stored = std::str::from_utf8(stored_hash_bytes)
        .map_err(|_| AppError::BadRequest("invalid stored hash bytes".into()))?;
    let parsed = PasswordHash::new(stored)
        .map_err(|_| AppError::BadRequest("invalid stored hash format".into()))?;
    let argon = Argon2::default();
    let cand = format!("{candidate}{pepper}");
    Ok(argon.verify_password(cand.as_bytes(), &parsed).is_ok())
}

pub async fn authenticate(db: &State<Db>, email: &str, password: &str) -> Result<Uuid, AppError> {
    let pep = get_pepper();
    let row = user_service::get_user_for_login(db, email)
        .await
        .map_err(|_| AppError::Unauthorized)?;

    if !row.is_active {
        return Err(AppError::Forbidden);
    }

    if !verify_password(&row.pass_hash, password, &pep)? {
        return Err(AppError::Unauthorized);
    }

    let session_id = Uuid::new_v4();
    let expires_at: NaiveDateTime =
        (Utc::now() + Duration::hours(DEFAULT_SESSIONS_AGE_HR as i64)).naive_utc();

    session_repo::insert_session(db, session_id, row.id, expires_at)
        .await
        .map_err(|e| map_sqlx_err(e, "Session"))?;

    println!("Created session {session_id} for user {}", row.email);

    Ok(session_id)
}

pub async fn remove_session(db: &State<Db>, cookie_id: Uuid) -> AppResult<()> {
    session_repo::delete_session(db, cookie_id)
        .await
        .map_err(|e| map_sqlx_err(e, "Session"))
}
