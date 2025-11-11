use crate::application::ports::input::mongo_console_port::MongoConsolePort;
use crate::application::services::db_consoles::projections::MongoResult;
use crate::application::services::user_context::{
    verify_service_or_admin_perms, UserContext,
};
use crate::application::services::utils::resolve_descriptor_by_container;
use crate::application::services::DbType;
use crate::shared::error::{AppError, AppResult};
use async_trait::async_trait;
use futures::TryStreamExt;
use mongodb::{bson::{self, doc, Document}, options::FindOptions, Client};
use rocket::form::FromForm;
use serde::Deserialize;
use serde_json::Value;
use std::sync::Arc;
use mongodb::bson::Bson;

pub struct MongoConsoleService {}

impl MongoConsoleService {
    pub fn new() -> Self {
        Self {}
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

        let username = std::env::var(db.db_user_env)
            .map_err(|_| AppError::Internal)?;
        let password = std::env::var(db.db_password_env)
            .map_err(|_| AppError::Internal)?;

        let uri = format!(
            "mongodb://{}:{}@{}:27017/{}?authSource=admin",
            username, password, db.db_host, db.db_name
        );

        log::info!("Connecting to Mongo: {}", uri);

        let client = Client::with_uri_str(&uri)
            .await
            .map_err(|e| {
                log::info!("Failed to connect to Mongo: {}", e);
                AppError::Internal
            })?;

        log::info!("Connected to Mongo");

        let db_handle = client.database(db.db_name);

        log::info!("Executing query");

        let json: Value = serde_json::from_str(&mongo_command).map_err(|e| {
            AppError::BadRequest(format!("Invalid Mongo command JSON: {}", e))
        })?;

        let command_doc: Document = bson::to_document(&json).map_err(|e| {
            AppError::BadRequest(format!("Invalid Mongo command BSON: {}", e))
        })?;

        let res: Document = db_handle
            .run_command(command_doc)
            .await
            .map_err(|e| {
                log::info!("Mongo command failed: {}", e);
                AppError::BadRequest(format!("Mongo command failed: {}", e))
            })?;

        log::info!("Mongo command executed successfully");

        // If the response contains a cursor with firstBatch, extract documents
        // This is when we do find() or aggregate()
        if let Ok(cursor) = res.get_document("cursor") {
            if let Ok(first_batch) = cursor.get_array("firstBatch") {
                let mut docs = Vec::with_capacity(first_batch.len());

                for b in first_batch {
                    let v = bson_to_serde_json(b.clone())?;
                    docs.push(v);
                }

                return Ok(MongoResult {
                    documents: docs,
                    count: docs.len() as i64,
                });
            }
        }

        // If the response contains n, nModified, or deletedCount, return the result
        // This is when we do update() or delete()
        let mut affected: i64 = 0;

        if let Ok(n) = res.get_i32("n") {
            affected = affected.max(n as i64);
        }
        if let Ok(nm) = res.get_i32("nModified") {
            affected = affected.max(nm as i64);
        }
        if let Ok(dc) = res.get_i64("deletedCount") {
            affected = affected.max(dc);
        }

        if affected > 0 {
            return Ok(MongoResult {
                documents: vec![],
                count: affected,
            });
        }

        // Otherwise, return the whole response as a single document
        // This is when we do findOne() or findOneAndDelete() (Also a fallback)
        let single = bson_to_serde_json(Bson::Document(res))?;

        Ok(MongoResult {
            documents: vec![single],
            count: 1,
        })
    }
}

fn bson_to_serde_json(b: bson::Bson) -> AppResult<Value> {
    serde_json::to_value(&b)
        .map_err(|e| AppError::Internal)
}
