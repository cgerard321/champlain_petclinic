use std::collections::HashSet;
use uuid::Uuid;

#[derive(Debug)]
pub struct UserEntity {
    pub user_id: Uuid,
    pub email: String,
    pub display_name: String,
    pub is_active: bool,
    pub roles: HashSet<RoleEntity>,
}

#[derive(Debug, Eq, PartialEq, Hash)]
pub struct RoleEntity {
    pub role_id: Uuid,
    pub name: String,
    pub description: Option<String>,
}
