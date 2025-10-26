use crate::application::ports::output::user_repo_port::DynUsersRepo;
use crate::application::usecases::users::create_user::create_user;
use crate::core::error::AppResult;
use crate::domain::models::user::{AuthenticatedUser, NewUser, User};
use rocket::serde::json::Json;
use rocket::{post, State};

#[post("/users", format = "application/json", data = "<new_user>")]
pub async fn add_user(db: &State<DynUsersRepo>, new_user: Json<NewUser>) -> AppResult<Json<User>> {
    create_user(db, new_user.into_inner()).await.map(Json)
}

#[get("/me")]
pub async fn me(user: AuthenticatedUser) -> String {
    format!("You are logged in as user {}", user.user_id)
}
