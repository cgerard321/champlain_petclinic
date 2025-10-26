use crate::bootstrap::Db;
use crate::core::error::{AppError, AppResult};
use crate::domain::models::user::LoginReq;
use crate::domain::usecases::auth::authenticate::authenticate;
use crate::domain::usecases::auth::logout::remove_session;
use rocket::http::{Cookie, CookieJar, SameSite};
use rocket::serde::json::Json;
use rocket::{http::Status, post, State};
use time::OffsetDateTime;
use uuid::Uuid;

#[post("/login", data = "<req>")]
pub async fn login(db: &State<Db>, req: Json<LoginReq>, jar: &CookieJar<'_>) -> AppResult<Status> {
    let new_session = authenticate(db, &req.email, &req.password)
        .await
        .map(Json)
        .map_err(|e| AppError::from(e))?;

    let expires_offset =
        OffsetDateTime::from_unix_timestamp(new_session.expires_at.and_utc().timestamp())
            .map_err(|_| AppError::UnprocessableEntity("invalid expiry timestamp".into()))?;

    let mut cookie = Cookie::build(("sid", new_session.id.to_string()))
        .http_only(true)
        .same_site(SameSite::Lax)
        .path("/")
        .expires(expires_offset)
        .build();

    // in production, add .set_secure(true) for HTTPS
    if cfg!(not(debug_assertions)) {
        cookie.set_secure(true);
    }

    jar.add_private(cookie);

    Ok(Status::NoContent)
}

#[post("/logout")]
pub async fn logout(jar: &CookieJar<'_>, db: &State<Db>) -> AppResult<Status> {
    if let Some(cookie) = jar.get_private("sid") {
        let _ = remove_session(db, Uuid::parse_str(cookie.value()).unwrap()).await;
        jar.remove_private(Cookie::from("sid"));
    }

    Ok(Status::NoContent)
}
