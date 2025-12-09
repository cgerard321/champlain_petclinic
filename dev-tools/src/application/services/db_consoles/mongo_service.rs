use crate::application::ports::input::mongo_console_port::MongoConsolePort;
use crate::application::ports::output::db_drivers_port::mongo_driver::DynMongoDriver;
use crate::application::ports::output::services_repo_port::DynServicesRepo;
use crate::application::services::db_consoles::projections::MongoResult;
use crate::application::services::user_context::{verify_service_or_admin_perms, UserContext};
use crate::domain::entities::service::DbType;
use crate::shared::error::{AppError, AppResult};
use async_trait::async_trait;
use std::collections::HashMap;
use std::sync::Arc;

pub struct MongoConsoleService {
    drivers: HashMap<String, Arc<DynMongoDriver>>, // key = db host (docker container name)
    service_repo: DynServicesRepo,
}

impl MongoConsoleService {
    pub fn new(
        drivers: HashMap<String, Arc<DynMongoDriver>>,
        service_repo: DynServicesRepo,
    ) -> Self {
        Self {
            drivers,
            service_repo,
        }
    }
}

#[async_trait]
impl MongoConsolePort for MongoConsoleService {
    async fn exec_mongo_on_service(
        &self,
        user_ctx: &UserContext,
        service: String,
        mongo_command: String,
        db_name: Option<String>,
    ) -> AppResult<MongoResult> {
        let desc = self.service_repo.get_service(&service).await?;

        log::info!("Resolved descriptor: {:?}", desc);
        verify_service_or_admin_perms(user_ctx, &desc)?;
        log::info!("Access granted");

        if desc.dbs.is_none() {
            return Err(AppError::BadRequest(format!(
                "Service '{}' has no associated database",
                service
            )));
        }

        let db = desc.get_db_by_name_or_default(db_name)?;

        if db.db_type != DbType::Mongo {
            return Err(AppError::BadRequest(format!(
                "Service '{}' does not use a Mongo database",
                service
            )));
        }

        log::info!("Executing query");

        self.drivers
            .get(&db.db_host)
            .ok_or_else(|| AppError::BadRequest(format!("Unknown container '{}'", service)))?
            .execute_query(&mongo_command, &db.db_name)
            .await
    }
}
