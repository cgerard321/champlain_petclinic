use crate::domain::entities::service::{ServiceDbEntity, ServiceEntity};

pub struct ServiceDbProjection {
    pub db_name: Option<String>,
    pub db_host: Option<String>,
    pub db_type: Option<String>,
}

impl From<&ServiceDbEntity> for ServiceDbProjection {
    fn from(desc: &ServiceDbEntity) -> Self {
        Self {
            db_name: Some(desc.db_name.clone()),
            db_host: Some(desc.db_host.clone()),
            db_type: Some(desc.db_type.to_string()),
        }
    }
}

pub struct ServiceProjection {
    pub name: String,
    pub docker_service: String,
    pub dbs: Option<Vec<ServiceDbProjection>>,
}

impl ServiceProjection {
    pub fn from_descriptor(desc: &ServiceEntity) -> Self {
        Self {
            name: desc.docker_service.to_string(),
            docker_service: desc.docker_service.to_string(),
            dbs: desc
                .dbs
                .as_ref()
                .map(|dbs| dbs.iter().map(|db| db.into()).collect()),
        }
    }
}
