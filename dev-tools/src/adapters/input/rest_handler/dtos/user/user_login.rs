use rocket::serde::Deserialize;
use veil::Redact;

// Technically you should not log the entire DTO but I wanted
// to try redact and the proper ways of hiding secrets.
#[derive(Redact, Deserialize)]
pub struct UserLoginDto {
    pub email: String,
    #[redact(fixed = 3)]
    pub password: String,
}
