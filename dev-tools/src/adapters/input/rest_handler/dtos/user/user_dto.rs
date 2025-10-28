use rocket::serde::{Deserialize, Serialize};
use uuid::Uuid;

#[derive(Debug, Deserialize, Serialize)]
pub struct UserDto {
    pub user_id: Uuid,
    pub email: String,
    pub display_name: String,
}
