use rocket::serde::{Deserialize, Serialize};
use uuid::Uuid;
use validator::Validate;
use veil::Redact;

#[derive(Debug, Deserialize, Serialize)]
pub struct UserResponseContract {
    pub user_id: Uuid,
    pub email: String,
    pub display_name: String,
    pub roles: Vec<Uuid>,
}

// Technically you should not log the entire DTO but I wanted
// to try redact and the proper ways of hiding secrets.
#[derive(Redact, Deserialize)]
pub struct UserLoginRequestContract {
    pub email: String,
    #[redact(fixed = 3)]
    pub password: String,
}

#[derive(Redact, Deserialize, Validate)]
pub struct UserSignUpRequestContract {
    pub email: String,
    #[redact(fixed = 3)]
    pub password: String,
    pub display_name: String,
    #[validate(length(min = 1, message = "At least one role must be provided"))]
    pub roles: Vec<Uuid>,
}
