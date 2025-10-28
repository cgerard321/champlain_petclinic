use crate::adapters::input::rest_handler::contracts::user_contracts::user::{UserResponseContract, UserSignUpRequestContract};
use crate::application::ports::input::user_port::DynUsersPort;
use crate::application::services::users::params::UserCreationParams;
use crate::core::error::AppResult;
use rocket::http::Status;
use rocket::serde::json::Json;
use rocket::{post, State};

#[post("/users", format = "application/json", data = "<new_user_dto>")]
pub async fn add_user(
    uc: &State<DynUsersPort>,
    new_user_dto: Json<UserSignUpRequestContract>,
) -> AppResult<(Status, Json<UserResponseContract>)> {
    let dto = new_user_dto.into_inner();
    let new_user_params = UserCreationParams::from(dto);

    Ok((
        Status::Created,
        Json(UserResponseContract::from(uc.create_user(new_user_params).await?)),
    ))
}
