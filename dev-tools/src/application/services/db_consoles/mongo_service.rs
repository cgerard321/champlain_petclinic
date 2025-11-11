use crate::application::ports::input::mongo_console_port::MongoConsolePort;
use crate::application::ports::output::db_drivers::mongo_driver::DynMongoDriver;
use crate::application::ports::output::db_drivers::mysql_driver::DynMySqlDriver;
use crate::application::services::db_consoles::projections::MongoResult;
use crate::application::services::user_context::{
    verify_service_or_admin_perms, UserContext,
};
use crate::application::services::utils::resolve_descriptor_by_container;
use crate::application::services::DbType;
use crate::shared::error::{AppError, AppResult};
use async_trait::async_trait;
use futures::TryStreamExt;
use mongodb::bson::Bson;
use mongodb::{bson::{self, doc, Document}, options::FindOptions, Client};
use rocket::form::FromForm;
use serde::Deserialize;
use serde_json::Value;
use std::collections::HashMap;
use std::sync::Arc;

pub struct MongoConsoleService {
    drivers: HashMap<&'static str, Arc<DynMongoDriver>>, // key = db host (docker container name)
}

impl MongoConsoleService {
    pub fn new(drivers: HashMap<&'static str, Arc<DynMongoDriver>>) -> Self {
        Self { drivers }
    }
}

#[derive(Debug, Deserialize)]
struct MongoQueryPayload {
    collection: String,
    #[serde(default)]
    filter: Value,
    #[serde(default)]
    limit: Option<i64>,
}

#[async_trait]
impl MongoConsolePort for MongoConsoleService {
    async fn exec_mongo_on_service(
        &self,
        user_ctx: &UserContext,
        service: String,
        mongo_command: String,
    ) -> AppResult<MongoResult> {
        let desc = resolve_descriptor_by_container(&service).ok_or_else(|| {
            log::info!("Unknown container '{}'", service);
            AppError::NotFound(format!("Unknown container '{}'", service))
        })?;

        log::info!("Resolved descriptor: {:?}", desc);
        verify_service_or_admin_perms(user_ctx, desc)?;
        log::info!("Access granted");

        let db = desc.db.as_ref().ok_or_else(|| {
            AppError::BadRequest(format!(
                "Service '{}' has no associated database",
                service
            ))
        })?;

        if db.db_type != DbType::Mongo {
            return Err(AppError::BadRequest(format!(
                "Service '{}' does not use a Mongo database",
                service
            )));
        }

        log::info!("Executing query");

        self.drivers
            .get(db.db_host)
            .ok_or_else(|| AppError::BadRequest(format!("Unknown container '{}'", service)))?
            .execute_query(&mongo_command, &db.db_name)
            .await
    }
}
