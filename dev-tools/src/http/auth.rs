use crate::auth::service;
use crate::core::config::DEFAULT_SESSIONS_AGE_HR;
use crate::db::database::Db;
use crate::users::user::LoginReq;
use rocket::http::{Cookie, CookieJar, SameSite};
use rocket::serde::json::Json;
use rocket::{http::Status, post, State};
use time::Duration as TDuration;
use uuid::Uuid;

#[post("/login", data = "<req>")]
pub async fn login(
    db: &State<Db>,
    req: Json<LoginReq>,
    jar: &CookieJar<'_>,
) -> Result<Status, Status> {
    let session_id = service::authenticate(db, &req.email, &req.password)
        .await
        .map_err(|e| e.status());

    let mut cookie = Cookie::build(("sid", session_id?.to_string()))
        .http_only(true)
        .same_site(SameSite::Lax)
        .path("/")
        .max_age(TDuration::hours(DEFAULT_SESSIONS_AGE_HR as i64))
        .build();

    // in production, add .set_secure(true) for HTTPS
    if cfg!(not(debug_assertions)) {
        cookie.set_secure(true);
    }

    jar.add_private(cookie);

    Ok(Status::NoContent)
}

#[post("/logout")]
pub async fn logout(jar: &CookieJar<'_>, db: &State<Db>) -> Result<Status, Status> {
    if let Some(cookie) = jar.get_private("sid") {
        if let Ok(session_id) = Uuid::parse_str(cookie.value())
            && let Err(_e) = service::remove_session(db, session_id).await
        {
            jar.remove_private(Cookie::from("sid"));
            return Err(Status::InternalServerError);
        }

        jar.remove_private(Cookie::from("sid"));
    }

    Ok(Status::NoContent)
}
