use crate::adapters::input::rest_handler::dtos::user::user_dto::UserDto;
use crate::adapters::input::rest_handler::dtos::user::user_login::UserLoginDto;
use crate::adapters::input::rest_handler::dtos::user::user_sign_up::UserSignUpDto;
use crate::application::services::auth::params::UserLoginParams;
use crate::application::services::users::params::UserCreationParams;
use crate::domain::entities::user::UserEntity;

impl From<UserEntity> for UserDto {
    fn from(value: UserEntity) -> Self {
        Self {
            user_id: value.user_id,
            email: value.email,
            display_name: value.display_name,
        }
    }
}

impl From<UserSignUpDto> for UserCreationParams {
    fn from(value: UserSignUpDto) -> Self {
        Self {
            email: value.email,
            display_name: value.display_name,
            password: value.password,
        }
    }
}

impl From<UserLoginDto> for UserLoginParams {
    fn from(value: UserLoginDto) -> Self {
        Self {
            email: value.email,
            password: value.password,
        }
    }
}
