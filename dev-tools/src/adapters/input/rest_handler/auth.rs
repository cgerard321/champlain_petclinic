use crate::application::ports::input::auth_port::DynAuthPort;
use crate::core::error::{AppError, AppResult};
use crate::domain::models::user::LoginReq;
use rocket::http::{Cookie, CookieJar, SameSite};
use rocket::serde::json::Json;
use rocket::{http::Status, post, State};
use time::OffsetDateTime;
use uuid::Uuid;

#[post("/login", data = "<req>")]
pub async fn login(
    uc: &State<DynAuthPort>,
    req: Json<LoginReq>,
    jar: &CookieJar<'_>,
) -> AppResult<Status> {
    let new_session = uc.authenticate(&req.email, &req.password).await.map(Json)?;

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
pub async fn logout(jar: &CookieJar<'_>, uc: &State<DynAuthPort>) -> AppResult<Status> {
    if let Some(cookie) = jar.get_private("sid") {
        if let Ok(session_id) = Uuid::parse_str(cookie.value()) {
            let _ = uc.logout(session_id).await;
        }
        jar.remove_private(Cookie::from("sid"));
    }

    Ok(Status::NoContent)
}
