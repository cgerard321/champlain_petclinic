use crate::application::services::ServiceDescriptor;

pub struct ServiceProjection {
    pub name: String,
    pub docker_service: String,
    pub db_name: Option<String>,
    pub db_host: Option<String>,
    pub db_type: Option<String>,
}

impl ServiceProjection {
    pub fn from_descriptor(desc: &ServiceDescriptor) -> Self {
        Self    {
            name: desc.docker_service.to_string(),
            docker_service: desc.docker_service.to_string(),
            db_name: desc.db.as_ref().map(|db| db.db_name.to_string()),
            db_host: desc.db.as_ref().map(|db| db.db_host.to_string()),
            db_type: desc.db.as_ref().map(|db| db.db_type.to_string()),
        }
    }
}