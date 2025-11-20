use crate::adapters::input::http::guards::auth_guard::AuthenticatedUser;
use crate::adapters::input::http::rest::contracts::user_contracts::user::{
    UserResponseContract, UserSignUpRequestContract,
};
use crate::application::ports::input::user_port::DynUsersPort;
use crate::application::services::users::params::UserCreationParams;
use crate::shared::error::AppResult;
use rocket::http::Status;
use rocket::serde::json::Json;
use rocket::{post, State};

#[post("/users", format = "application/json", data = "<new_user>")]
pub async fn add_user(
    port: &State<DynUsersPort>,
    new_user: Json<UserSignUpRequestContract>,
    user: AuthenticatedUser,
) -> AppResult<(Status, Json<UserResponseContract>)> {
    let auth_context = user.into();

    let new_user_params = UserCreationParams::from(new_user.into_inner());

    Ok((
        Status::Created,
        Json(UserResponseContract::from(
            port.create_user(new_user_params, auth_context).await?,
        )),
    ))
}
