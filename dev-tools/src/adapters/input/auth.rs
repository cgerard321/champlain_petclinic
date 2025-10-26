use crate::bootstrap::Db;
use crate::domain::models::user::LoginReq;
use crate::domain::usecases::auth::authenticate::authenticate;
use crate::domain::usecases::auth::logout::remove_session;
use rocket::http::{Cookie, CookieJar, SameSite};
use rocket::serde::json::Json;
use rocket::{http::Status, post, Request, State};
use uuid::Uuid;

#[post("/login", data = "<req>")]
pub async fn login(
    db: &State<Db>,
    req: Json<LoginReq>,
    jar: &CookieJar<'_>,
) -> Result<Status, Status> {
    let session_id = authenticate(db, &req.email, &req.password)
        .await
        .map(Json)
        .map_err(|e| e.status());

    let mut cookie = Cookie::build(("sid", session_id?.to_string()))
        .http_only(true)
        .same_site(SameSite::Lax)
        .path("/")
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
        let _ = remove_session(&*db, Uuid::parse_str(cookie.value()).unwrap()).await;
        jar.remove_private(Cookie::from("sid"));
    }

    Ok(Status::NoContent)
}
