use rocket::serde::Deserialize;

#[derive(Debug, Deserialize)]
pub struct UserSignUpDto {
    pub email: String,
    pub password: String,
    pub display_name: String,
}
