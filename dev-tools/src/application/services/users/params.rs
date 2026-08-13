use std::collections::HashSet;
use uuid::Uuid;

pub struct UserCreationParams {
    pub email: String,
    pub password: String,
    pub display_name: String,
    pub roles: HashSet<Uuid>,
}
