use crate::adapters::input::http::graphql::contracts::service::ServiceResponseContract;
use crate::application::services::docker::projections::ServiceProjection;

impl From<ServiceProjection> for ServiceResponseContract {
    fn from(src: ServiceProjection) -> Self {
        ServiceResponseContract {
            name: src.name,
            docker_service: src.docker_service,
            db_name: src.db_name,
            db_host: src.db_host,
            db_type: src.db_type,
        }
    }
}
