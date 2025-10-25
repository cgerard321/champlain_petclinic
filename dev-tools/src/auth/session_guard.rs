use crate::auth::repo::find_session_by_id;
use crate::db::database::Db;
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

        let db: &State<Db> = match req.guard::<&State<Db>>().await {
            Outcome::Success(db) => db,
            Outcome::Forward(_) => return Outcome::Error((Status::InternalServerError, ())),
            Outcome::Error((_, _)) => {
                return Outcome::Error((Status::InternalServerError, ()));
            }
        };

        let Some(cookie) = jar.get_private("sid") else {
            return Outcome::Error((Status::Unauthorized, ()));
        };

        let Ok(sid) = Uuid::parse_str(cookie.value()) else {
            return Outcome::Error((Status::Unauthorized, ()));
        };

        match find_session_by_id(db, sid).await {
            Ok(session) => Outcome::Success(AuthenticatedUser {
                user_id: session.user_id,
            }),
            Err(sqlx::Error::RowNotFound) => Outcome::Error((Status::Unauthorized, ())),
            Err(_) => Outcome::Error((Status::InternalServerError, ())),
        }
    }
}

pub struct AuthenticatedUser {
    pub user_id: Uuid,
}
