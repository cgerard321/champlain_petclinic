use crate::application::ports::input::auth_port::DynAuthPort;
use crate::domain::entities::user::RoleEntity;
use crate::shared::error::AppError;
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

impl AuthenticatedUser {
    pub fn into(self) -> crate::application::services::user_context::UserContext {
        crate::application::services::user_context::UserContext {
            user_id: self.user_id,
            roles: self.roles,
        }
    }
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

        let port = match req.guard::<&State<DynAuthPort>>().await {
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

        match port.validate_session(sid).await {
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
            Err(AppError::Unauthorized) | Err(AppError::NotFound(_)) => {
                log::info!("User isn't authenticated");
                Outcome::Error((Status::Unauthorized, ()))
            }
            Err(AppError::Forbidden) => {
                log::info!("User is authenticated but not authorized");
                Outcome::Error((Status::Forbidden, ()))
            }
            Err(error) => {
                log::error!("validate_session internal error: {:?}", error);
                Outcome::Error((Status::InternalServerError, ()))
            }
        }
    }
}
