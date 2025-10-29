use crate::application::ports::output::auth_repo_port::DynAuthRepo;
use crate::application::ports::output::user_repo_port::DynUsersRepo;
use crate::application::services::auth::utils::verify_password;
use crate::core::config::SESSION_EXPIRATION_HR;
use crate::core::error::{AppError, AppResult};
use crate::core::utils::auth::get_pepper;
use crate::domain::entities::session::SessionEntity;
use chrono::{Duration, NaiveDateTime, Utc};
use uuid::Uuid;

pub async fn authenticate(
    auth_db: &DynAuthRepo,
    user_db: &DynUsersRepo,
    email: &str,
    password: &str,
) -> AppResult<SessionEntity> {
    let pep = get_pepper();
    let auth_obj = user_db
        .get_user_auth_by_email_for_login(email)
        .await
        .map_err(|_| AppError::Unauthorized)?;

    if !auth_obj.user.is_active {
        return Err(AppError::Forbidden);
    }

    if !verify_password(&auth_obj.pass_hash, password, &pep)? {
        return Err(AppError::Unauthorized);
    }

    let session_id = Uuid::new_v4();
    let expires_at: NaiveDateTime =
        (Utc::now() + Duration::hours(SESSION_EXPIRATION_HR)).naive_utc();

    let new_session = auth_db
        .insert_session(session_id, auth_obj.user.user_id, expires_at)
        .await
        .map_err(|_e| AppError::Internal)?;

    Ok(SessionEntity {
        id: new_session.id,
        user_id: new_session.user_id,
        created_at: new_session.created_at,
        expires_at: new_session.expires_at,
    })
}
