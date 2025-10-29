use rocket::serde::{Deserialize, Serialize};
use uuid::Uuid;
use veil::Redact;

#[derive(Debug, Deserialize, Serialize)]
pub struct UserResponseContract {
    pub user_id: Uuid,
    pub email: String,
    pub display_name: String,
}

// Technically you should not log the entire DTO but I wanted
// to try redact and the proper ways of hiding secrets.
#[derive(Redact, Deserialize)]
pub struct UserLoginRequestContract {
    pub email: String,
    #[redact(fixed = 3)]
    pub password: String,
}

#[derive(Redact, Deserialize)]
pub struct UserSignUpRequestContract {
    pub email: String,
    #[redact(fixed = 3)]
    pub password: String,
    pub display_name: String,
}
