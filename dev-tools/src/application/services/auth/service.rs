use crate::application::ports::input::auth_port::AuthPort;
use crate::application::ports::output::auth_repo_port::DynAuthRepo;
use crate::application::ports::output::crypto_port::DynCrypto;
use crate::application::ports::output::user_repo_port::DynUsersRepo;
use crate::application::services::auth::authenticate::authenticate;
use crate::application::services::auth::find_session_by_id::find_session_by_id;
use crate::application::services::auth::logout::remove_session;
use crate::application::services::auth::params::UserLoginParams;
use crate::domain::entities::session::SessionEntity;
use crate::domain::entities::user::UserEntity;
use crate::shared::error::{AppError, AppResult};
use uuid::Uuid;

pub struct AuthService {
    auth_repo: DynAuthRepo,
    users_repo: DynUsersRepo,
    crypto: DynCrypto,
}
impl AuthService {
    pub fn new(auth_repo: DynAuthRepo, users_repo: DynUsersRepo, crypto: DynCrypto) -> Self {
        Self {
            auth_repo,
            users_repo,
            crypto,
        }
    }
}

#[async_trait::async_trait]
impl AuthPort for AuthService {
    async fn authenticate(&self, user_login_params: UserLoginParams) -> AppResult<SessionEntity> {
        authenticate(
            &self.crypto,
            &self.auth_repo,
            &self.users_repo,
            user_login_params.email.as_str(),
            user_login_params.password.as_str(),
        )
        .await
    }

    async fn logout(&self, session_id: Uuid) -> AppResult<()> {
        remove_session(&self.auth_repo, session_id).await
    }

    async fn validate_session(&self, session_id: Uuid) -> AppResult<UserEntity> {
        log::info!("Validating session {}", session_id);
        let session = find_session_by_id(&self.auth_repo, session_id)
            .await
            .map_err(|e| match e {
                AppError::NotFound(_) => {
                    log::info!("Session {} not found", session_id);
                    AppError::Unauthorized
                }
                _ => e,
            })?;

        log::info!("Session found: {:?}", session);

        if session.is_expired() {
            log::info!("Session {} is expired", session_id);
            return Err(AppError::Unauthorized);
        }

        log::info!("Session {} is valid", session_id);

        Ok(self.users_repo.get_user_by_id(session.user_id).await?)
    }
}
