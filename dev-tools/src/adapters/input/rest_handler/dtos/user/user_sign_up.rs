use rocket::serde::Deserialize;
use veil::Redact;

#[derive(Redact, Deserialize)]
pub struct UserSignUpDto {
    pub email: String,
    #[redact(fixed = 3)]
    pub password: String,
    pub display_name: String,
}
