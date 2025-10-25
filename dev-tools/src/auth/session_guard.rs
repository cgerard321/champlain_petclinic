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
        let db = req.guard::<&State<Db>>().await.unwrap();

        let Some(cookie) = jar.get_private("sid") else {
            return Outcome::Error((Status::Unauthorized, ()));
        };

        let Ok(sid) = Uuid::parse_str(cookie.value()) else {
            return Outcome::Error((Status::Unauthorized, ()));
        };

        match find_session_by_id(db, sid).await {
            Ok(Some(session)) => Outcome::Success(AuthenticatedUser {
                user_id: session.user_id,
            }),
            _ => Outcome::Error((Status::Unauthorized, ())),
        }
    }
}


pub struct AuthenticatedUser {
    pub user_id: Uuid,
}