use crate::domain::entities::service::{ServiceDbEntity, ServiceEntity};
use crate::shared::error::AppResult;

#[async_trait::async_trait]
pub trait ServicesRepoPort: Send + Sync {
    async fn list_services(&self) -> AppResult<Vec<ServiceEntity>>;

    async fn get_service(&self, service_name: &str) -> AppResult<ServiceEntity>;

    async fn get_service_dbs(&self, service_name: &str) -> AppResult<Option<Vec<ServiceDbEntity>>>;

    async fn add_service(&self, service: &ServiceEntity) -> AppResult<()>;
}

pub type DynServicesRepo = std::sync::Arc<dyn ServicesRepoPort>;
