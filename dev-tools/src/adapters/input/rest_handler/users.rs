use crate::adapters::input::rest_handler::dtos::user::user_dto::UserDto;
use crate::adapters::input::rest_handler::dtos::user::user_sign_up::UserSignUpDto;
use crate::application::ports::input::user_port::DynUsersPort;
use crate::application::services::users::params::UserCreationParams;
use crate::core::error::AppResult;
use crate::domain::entities::user::UserEntity;
use rocket::http::Status;
use rocket::serde::json::Json;
use rocket::{post, State};

#[post("/users", format = "application/json", data = "<new_user_dto>")]
pub async fn add_user(
    uc: &State<DynUsersPort>,
    new_user_dto: Json<UserSignUpDto>,
) -> AppResult<(Status, Json<UserDto>)> {
    let dto = new_user_dto.into_inner();
    let new_user = UserCreationParams {
        email: dto.email,
        password: dto.password,
        display_name: dto.display_name,
    };

    uc.create_user(new_user)
        .await
        .map(|user_entity: UserEntity| {
            (
                Status::Created,
                Json(UserDto {
                    user_id: user_entity.user_id,
                    email: user_entity.email,
                    display_name: user_entity.display_name,
                }),
            )
        })
}
