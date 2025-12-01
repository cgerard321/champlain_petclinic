use crate::adapters::input::http::graphql::contracts::service::{
    ServiceDbResponseContract, ServiceResponseContract,
};
use crate::application::services::docker::projections::{ServiceDbProjection, ServiceProjection};

impl From<ServiceProjection> for ServiceResponseContract {
    fn from(src: ServiceProjection) -> Self {
        ServiceResponseContract {
            name: src.name,
            docker_service: src.docker_service,
            dbs: src.dbs.map(|dbs| {
                dbs.into_iter()
                    .map(ServiceDbResponseContract::from)
                    .collect()
            }),
        }
    }
}

impl From<ServiceDbProjection> for ServiceDbResponseContract {
    fn from(value: ServiceDbProjection) -> Self {
        Self {
            db_name: value.db_name,
            db_host: value.db_host,
            db_type: value.db_type,
        }
    }
}
