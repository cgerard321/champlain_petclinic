use crate::application::ports::input::auth_port::DynAuthPort;
use crate::core::error::AppError;
use crate::domain::models::user::AuthenticatedUser;
use rocket::{
    http::Status,
    request::{FromRequest, Outcome, Request},
    State,
};
use uuid::Uuid;

#[rocket::async_trait]
impl<'r> FromRequest<'r> for AuthenticatedUser {
    type Error = ();

    async fn from_request(req: &'r Request<'_>) -> Outcome<Self, Self::Error> {
        let jar = req.cookies();
        let uc = match req.guard::<&State<DynAuthPort>>().await {
            Outcome::Success(state) => state,
            Outcome::Error(f) => return Outcome::Error(f),
            Outcome::Forward(_) => return Outcome::Error((Status::InternalServerError, ())),
        };

        let Some(cookie) = jar.get_private("sid") else {
            return Outcome::Error((Status::Unauthorized, ()));
        };

        let Ok(sid) = Uuid::parse_str(cookie.value()) else {
            return Outcome::Error((Status::Unauthorized, ()));
        };

        match uc.validate_session(sid).await {
            Ok(user_id) => Outcome::Success(AuthenticatedUser { user_id }),
            Err(AppError::NotFound(_)) | Err(AppError::Unauthorized) => {
                Outcome::Error((Status::Unauthorized, ()))
            }
            Err(_) => Outcome::Error((Status::InternalServerError, ())),
        }
    }
}
