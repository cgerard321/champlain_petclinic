use crate::adapters::input::rest_handler::dtos::user::user::UserDto;
use crate::adapters::input::rest_handler::dtos::user::user_sign_up::UserSignUpDto;
use crate::application::ports::input::user_port::DynUsersPort;
use crate::core::error::AppResult;
use crate::domain::entities::user::UserEntity;
use rocket::serde::json::Json;
use rocket::{post, State};
use crate::application::services::users::params::UserCreationParams;

#[post("/users", format = "application/json", data = "<new_user_dto>")]
pub async fn add_user(
    uc: &State<DynUsersPort>,
    new_user_dto: Json<UserSignUpDto>,
) -> AppResult<Json<UserDto>> {
    let new_user = UserCreationParams {
        email: new_user_dto.email.clone(),
        password: new_user_dto.password.clone(),
        display_name: new_user_dto.display_name.clone(),
    };

    uc.create_user(new_user)
        .await
        .map(|user_entity: UserEntity| {
            Json(UserDto {
                user_id: user_entity.user_id,
                email: user_entity.email,
                display_name: user_entity.display_name,
            })
        })
}
