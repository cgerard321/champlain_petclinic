use crate::adapters::output::mysql::model::role::Role;
use crate::domain::entities::user::RoleEntity;
use uuid::Uuid;

impl From<Role> for RoleEntity {
    fn from(role: Role) -> Self {
        Self {
            role_id: Uuid::from(role.id),
            name: role.code,
            description: role.description,
        }
    }
}
