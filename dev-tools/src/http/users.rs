use crate::core::error::AppResult;
use crate::db::database::Db;
use crate::users::service;
use crate::users::user::{AuthenticatedUser, NewUser, User};
use rocket::serde::json::Json;
use rocket::{post, State};

#[post("/users", format = "application/json", data = "<new_user>")]
pub async fn add_user(db: &State<Db>, new_user: Json<NewUser>) -> AppResult<Json<User>> {
    service::insert_user(db, new_user.into_inner())
        .await
        .map(Json)
}

#[get("/me")]
pub async fn me(user: AuthenticatedUser) -> String {
    format!("You are logged in as user {}", user.user_id)
}
