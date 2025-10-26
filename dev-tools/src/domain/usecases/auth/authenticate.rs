use crate::adapters::output::mysql::auth_repo;
use crate::adapters::output::mysql::users_repo::get_user_auth_by_email;
use crate::bootstrap::Db;
use crate::core::config::SESSION_EXPIRATION_HR;
use crate::core::error::{AppError, AppResult};
use crate::core::utils::auth::get_pepper;
use crate::domain::models::session::Session;
use crate::domain::usecases::auth::utils::verify_password;
use chrono::{Duration, NaiveDateTime, Utc};
use rocket::State;
use uuid::Uuid;

pub async fn authenticate(db: &State<Db>, email: &str, password: &str) -> AppResult<Session> {
    let pep = get_pepper();
    let row = get_user_auth_by_email(db, email)
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
        (Utc::now() + Duration::hours(SESSION_EXPIRATION_HR)).naive_utc();

    let new_session = auth_repo::insert_session(db, session_id, row.id, expires_at)
        .await
        .map_err(|_e| AppError::Internal)?;

    println!("Created session {session_id} for user {}", row.email);

    Ok(Session {
        id: new_session.id,
        user_id: new_session.user_id,
        created_at: new_session.created_at,
        expires_at: new_session.expires_at,
    })
}
