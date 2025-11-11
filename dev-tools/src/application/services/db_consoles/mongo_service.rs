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
        mongo_query: String,
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

        let payload: MongoQueryPayload = serde_json::from_str(&mongo_query).map_err(|e| {
            AppError::BadRequest(format!("Invalid mongoQuery JSON: {}", e))
        })?;

        let filter_doc = if payload.filter.is_null() {
            Document::new()
        } else {
            bson::to_document(&payload.filter).map_err(|e| {
                AppError::BadRequest(format!("Invalid filter BSON: {}", e))
            })?
        };

        // Build MongoDB URI from descriptor
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
        let coll = db_handle.collection::<Document>(&payload.collection);

        log::info!("Executing query");

        let mut cursor = coll
            .find(filter_doc)
            .await
            .map_err(|e| {
                log::info!("Query execution failed: {}", e);
                AppError::Internal
            })?;

        log::info!("Query executed");

        let mut docs = Vec::new();
        while let Some(doc) = cursor
            .try_next()
            .await
            .map_err(|e| AppError::Internal)?
        {
            let json_val = bson::to_bson(&doc)
                .map_err(|e| AppError::Internal)?;
            let json_val = bson_to_serde_json(json_val)?;
            docs.push(json_val);
        }

        let count = docs.len() as i64;

        log::info!("Query returned {} documents", count);

        Ok(MongoResult {
            collection: payload.collection,
            documents: docs,
            count,
        })
    }
}

fn bson_to_serde_json(b: bson::Bson) -> AppResult<Value> {
    serde_json::to_value(&b)
        .map_err(|e| AppError::Internal)
}
