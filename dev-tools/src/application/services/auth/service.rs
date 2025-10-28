use crate::application::ports::input::auth_port::AuthPort;
use crate::application::ports::output::auth_repo_port::DynAuthRepo;
use crate::application::ports::output::user_repo_port::DynUsersRepo;
use crate::application::services::auth::authenticate::authenticate;
use crate::application::services::auth::find_session_by_id::find_session_by_id;
use crate::application::services::auth::logout::remove_session;
use crate::application::services::auth::params::UserLoginParams;
use crate::core::error::AppResult;
use crate::domain::entities::session::SessionEntity;
use uuid::Uuid;

pub struct AuthService {
    auth_repo: DynAuthRepo,
    users_repo: DynUsersRepo,
}
impl AuthService {
    pub fn new(auth_repo: DynAuthRepo, users_repo: DynUsersRepo) -> Self {
        Self {
            auth_repo,
            users_repo,
        }
    }
}

#[async_trait::async_trait]
impl AuthPort for AuthService {
    async fn authenticate(&self, user_login_params: UserLoginParams) -> AppResult<SessionEntity> {
        authenticate(
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

    async fn validate_session(&self, session_id: Uuid) -> AppResult<Uuid> {
        Ok(find_session_by_id(&self.auth_repo, session_id)
            .await?
            .user_id)
    }
}
