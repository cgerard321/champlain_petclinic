use crate::application::ports::input::sql_console_port::SqlConsolePort;
use crate::application::ports::output::db_drivers_port::sql_driver::DynSqlDriver;
use crate::application::ports::output::services_repo_port::DynServicesRepo;
use crate::application::services::db_consoles::projections::SqlResult;
use crate::application::services::user_context::{verify_service_or_admin_perms, UserContext};
use crate::domain::entities::service::DbType;
use crate::shared::error::{AppError, AppResult};
use std::collections::HashMap;
use std::sync::Arc;

pub struct SqlConsoleService {
    drivers: HashMap<String, Arc<DynSqlDriver>>, // key = db host (docker container name)
    service_repo: DynServicesRepo,
}

impl SqlConsoleService {
    pub fn new(
        drivers: HashMap<String, Arc<DynSqlDriver>>,
        service_repo: DynServicesRepo,
    ) -> Self {
        Self {
            drivers,
            service_repo,
        }
    }
}

#[async_trait::async_trait]
impl SqlConsolePort for SqlConsoleService {
    async fn exec_sql_on_service(
        &self,
        user_ctx: &UserContext,
        service: String,
        sql: String,
        db_name: Option<String>,
    ) -> AppResult<SqlResult> {
        let desc = self.service_repo.get_service(&service).await?;

        log::info!("Resolved descriptor: {:?}", desc);
        verify_service_or_admin_perms(user_ctx, &desc)?;
        log::info!("Access granted");

        let db = desc.get_db_by_name_or_default(db_name)?;

        if db.db_type != DbType::MySQL && db.db_type != DbType::Postgres {
            return Err(AppError::BadRequest(format!(
                "Service '{}' does not use a SQL database",
                service
            )));
        }

        log::info!("Executing query");

        let driver = self
            .drivers
            .get(&db.db_host)
            .ok_or_else(|| AppError::BadRequest(format!("Unknown container '{}'", service)))?;

        log::info!("Using driver for db host: {}", db.db_host);
        driver.execute_query(&sql).await
    }
}
