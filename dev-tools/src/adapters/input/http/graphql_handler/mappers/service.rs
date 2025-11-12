use crate::adapters::input::http::graphql_handler::contracts::service::ServiceResponseContract;
use crate::application::services::{DbType, ServiceDescriptor};
use crate::application::services::docker::projections::ServiceProjection;

impl From<ServiceProjection> for ServiceResponseContract {
    fn from(src: ServiceProjection) -> Self {
        ServiceResponseContract {
            name: src.docker_service.to_string(),
            docker_service: src.docker_service.to_string(),
            db_name: src.db.as_ref().map(|db| db.db_name.to_string()),
            db_host: src.db.as_ref().map(|db| db.db_host.to_string()),
            db_type: src.db.as_ref().map(|db| match db.db_type {
                DbType::Mongo => "MONGO".into(),
                DbType::Sql => "SQL".into(),
            }),
        }
    }
}
