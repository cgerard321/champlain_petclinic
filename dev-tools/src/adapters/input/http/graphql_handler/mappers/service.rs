use crate::adapters::input::http::graphql_handler::contracts::service::ServiceResponseContract;
use crate::application::services::ServiceDescriptor;

impl From<ServiceDescriptor> for ServiceResponseContract {
    fn from(src: ServiceDescriptor) -> Self {
        ServiceResponseContract {
            name: src.docker_service.to_string(),
            docker_service: src.docker_service.to_string(),
            db_name: src.db.as_ref().map(|db| db.db_name.clone().to_string()),
            db_host: src.db.as_ref().map(|db| db.db_host.clone().to_string()),
            db_type: Default::default(),
        }
    }
}