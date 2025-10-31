use crate::adapters::input::rest_handler::contracts::user_contracts::user::{
    UserLoginRequestContract, UserResponseContract, UserSignUpRequestContract,
};
use crate::application::services::auth::params::UserLoginParams;
use crate::application::services::users::params::UserCreationParams;
use crate::domain::entities::user::UserEntity;
use std::collections::HashSet;

impl From<UserEntity> for UserResponseContract {
    fn from(value: UserEntity) -> Self {
        Self {
            user_id: value.user_id,
            email: value.email,
            display_name: value.display_name,
            // Only return the role IDs not the whole role entity.
            roles: value.roles.into_iter().map(|r| r.role_id).collect(),
        }
    }
}

impl From<UserSignUpRequestContract> for UserCreationParams {
    fn from(value: UserSignUpRequestContract) -> Self {
        Self {
            email: value.email,
            display_name: value.display_name,
            password: value.password,
            roles: HashSet::from_iter(value.roles),
        }
    }
}

impl From<UserLoginRequestContract> for UserLoginParams {
    fn from(value: UserLoginRequestContract) -> Self {
        Self {
            email: value.email,
            password: value.password,
        }
    }
}
