use crate::application::ports::input::sql_console_port::SqlConsolePort;
use crate::application::ports::output::db_drivers::mysql_driver::DynMySqlDriver;
use crate::application::services::db_consoles::projections::SqlResult;
use crate::application::services::user_context::{
    require_all, require_any, verify_service_or_admin_perms, UserContext,
};
use crate::application::services::utils::resolve_descriptor_by_container;
use crate::application::services::DbType;
use crate::shared::config::{ADMIN_ROLE_UUID, READER_ROLE_UUID};
use crate::shared::error::{AppError, AppResult};
use std::cmp::PartialEq;
use std::collections::HashMap;
use std::sync::Arc;

pub struct SqlConsoleService {
    drivers: HashMap<&'static str, Arc<DynMySqlDriver>>, // key = db host (docker container name)
}

impl SqlConsoleService {
    pub fn new(drivers: HashMap<&'static str, Arc<DynMySqlDriver>>) -> Self {
        Self { drivers }
    }
}


#[async_trait::async_trait]
impl SqlConsolePort for SqlConsoleService {
    async fn exec_sql_on_service(
        &self,
        user_ctx: &UserContext,
        service: String,
        sql: String,
    ) -> AppResult<SqlResult> {
        let desc = resolve_descriptor_by_container(&service).ok_or_else(|| {
            log::info!("Unknown container '{}'", service);
            AppError::NotFound(format!("Unknown container '{}'", service))
        })?;

        log::info!("Resolved descriptor: {:?}", desc);
        verify_service_or_admin_perms(user_ctx, desc)?;
        log::info!("Access granted");

        let db_host = desc.db.as_ref().map_or("", |db| db.db_host);

        if db_host.is_empty() {
            return Err(AppError::BadRequest(format!(
                "Service '{}' has no associated database",
                service
            )));
        }

        let db = desc.db.as_ref().ok_or_else(|| {
            log::info!("Service '{}' has no associated database", service);
            AppError::BadRequest(format!("Service '{}' has no associated database", service))
        })?;

        if db.db_type != DbType::Sql {
            return Err(AppError::BadRequest(format!(
                "Service '{}' does not use a SQL database",
                service
            )));
        }

        let driver = self
            .drivers
            .get(db_host)
            .ok_or_else(|| AppError::BadRequest(format!("Unknown container '{}'", service)))?;

        log::info!("Using driver for db host: {}", db_host);
        driver.execute_query(&sql).await
    }
}
