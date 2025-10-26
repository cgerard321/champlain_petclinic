use crate::adapters::output::mysql::auth_repo;
use crate::adapters::output::mysql::users_repo::get_user_auth_by_email;
use crate::bootstrap::Db;
use crate::core::error::AppError;
use crate::core::utils::auth::get_pepper;
use crate::domain::usecases::auth::utils::verify_password;
use chrono::{Duration, NaiveDateTime, Utc};
use rocket::State;
use uuid::Uuid;

pub async fn authenticate(db: &State<Db>, email: &str, password: &str) -> Result<Uuid, AppError> {
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
    let expires_at: NaiveDateTime = (Utc::now() + Duration::days(1)).naive_utc();

    auth_repo::insert_session(db, session_id, row.id, expires_at)
        .await
        .map_err(|_e| AppError::Internal)?;

    println!("Created session {session_id} for user {}", row.email);

    Ok(session_id)
}
