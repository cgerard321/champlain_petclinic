use crate::application::ports::output::db_drivers::mongo_driver::MongoDriverPort;
use crate::application::services::db_consoles::projections::MongoResult;
use crate::shared::error::{AppError, AppResult};
use mongodb::bson::{Bson, Document};
use mongodb::{bson, Client};
use serde_json::Value;
use sqlx::mysql::{MySqlConnectOptions, MySqlSslMode};
use sqlx::MySqlPool;

pub struct MongoDriver {
    client: Client,
}

impl MongoDriver {
    pub async fn new(url: &str) -> Self {
        let client = Client::with_uri_str(url).await.expect("Failed to create MongoDB client");
        Self { client }
    }
}

#[async_trait::async_trait]
impl MongoDriverPort for MongoDriver {
    async fn execute_query(
        &self,
        mongo_command: &str,
        database_name: &str,
    ) -> AppResult<MongoResult> {
        let db_handle = self.client.database(database_name);

        let json: Value = serde_json::from_str(&mongo_command)
            .map_err(|e| AppError::BadRequest(format!("Invalid Mongo command JSON: {}", e)))?;

        let command_doc: Document = bson::to_document(&json)
            .map_err(|e| AppError::BadRequest(format!("Invalid Mongo command BSON: {}", e)))?;

        let res: Document = db_handle.run_command(command_doc).await.map_err(|e| {
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

                let len = docs.len();

                return Ok(MongoResult {
                    bson: docs,
                    affected_count: len as i64,
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

        log::info!("Mongo command affected {} documents", affected);
        if affected > 0 {
            return Ok(MongoResult {
                bson: vec![],
                affected_count: affected,
            });
        }

        // We return a single document, this will include the mongo response
        let single = bson_to_serde_json(Bson::Document(res))?;

        Ok(MongoResult {
            bson: vec![single],
            affected_count: affected,
        })
    }
}

fn bson_to_serde_json(b: Bson) -> AppResult<Value> {
    serde_json::to_value(&b).map_err(|e| AppError::Internal)
}
