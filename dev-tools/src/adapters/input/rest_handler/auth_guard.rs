use crate::application::ports::input::auth_port::DynAuthPort;
use crate::core::error::AppError;
use crate::domain::entities::user::RoleEntity;
use rocket::http::CookieJar;
use rocket::{
    http::Status,
    request::{FromRequest, Outcome, Request},
    State,
};
use std::collections::HashSet;
use uuid::Uuid;

#[derive(Debug)]
#[allow(dead_code)]
pub struct AuthenticatedUser {
    pub user_id: Uuid,
    pub roles: HashSet<Uuid>,
}

#[rocket::async_trait]
impl<'r> FromRequest<'r> for AuthenticatedUser {
    type Error = ();

    async fn from_request(req: &'r Request<'_>) -> Outcome<Self, Self::Error> {
        log::info!("Authenticating user");

        let jar = match req.guard::<&CookieJar>().await {
            Outcome::Success(j) => j,
            Outcome::Error(e) => {
                log::error!("CookieJar guard failed: {:?}", e);
                return Outcome::Error((Status::InternalServerError, ()));
            }
            Outcome::Forward(_) => {
                log::error!("CookieJar guard forwarded");
                return Outcome::Error((Status::InternalServerError, ()));
            }
        };

        let uc = match req.guard::<&State<DynAuthPort>>().await {
            Outcome::Success(state) => state,
            Outcome::Error(error) => {
                log::error!("DynAuthPort state guard failed: {:?}", error);
                return Outcome::Error((Status::InternalServerError, ()));
            }
            Outcome::Forward(_) => {
                log::error!("DynAuthPort state guard forwarded");
                return Outcome::Error((Status::InternalServerError, ()));
            }
        };

        let Some(cookie) = jar.get_private("sid") else {
            log::info!("No 'sid' cookie: unauthorized");
            return Outcome::Error((Status::Unauthorized, ()));
        };

        let sid = match Uuid::parse_str(cookie.value()) {
            Ok(session_id) => session_id,
            Err(error) => {
                log::info!("Invalid sid in cookie: {}", error);
                return Outcome::Error((Status::Unauthorized, ()));
            }
        };

        match uc.validate_session(sid).await {
            Ok(user) => {
                log::info!("User {} authenticated", user.email);
                let role_ids: HashSet<Uuid> = user
                    .roles
                    .into_iter()
                    .map(|r: RoleEntity| r.role_id)
                    .collect();

                Outcome::Success(AuthenticatedUser {
                    user_id: user.user_id,
                    roles: role_ids,
                })
            }
            Err(AppError::Unauthorized) | Err(AppError::Forbidden) | Err(AppError::NotFound(_)) => {
                log::info!("User isn't authenticated");
                Outcome::Error((Status::Unauthorized, ()))
            }
            Err(error) => {
                log::error!("validate_session internal error: {:?}", error);
                Outcome::Error((Status::InternalServerError, ()))
            }
        }
    }
}

#[inline]
pub fn require_any(user: &AuthenticatedUser, required: &[Uuid]) -> Result<(), AppError> {
    if required.iter().any(|r| user.roles.contains(r)) {
        Ok(())
    } else {
        Err(AppError::Forbidden)
    }
}

// Not used currently, but may be useful in the future
#[inline]
#[allow(dead_code)]
pub fn require_all(user: &AuthenticatedUser, required: &[Uuid]) -> Result<(), AppError> {
    if required.iter().all(|r| user.roles.contains(r)) {
        Ok(())
    } else {
        Err(AppError::Forbidden)
    }
}
