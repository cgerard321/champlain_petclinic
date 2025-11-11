use crate::application::ports::output::db_drivers::sql_driver::SqlDriverPort;
use crate::application::services::db_consoles::projections::SqlResult;
use crate::shared::error::{AppError, AppResult};
use async_trait::async_trait;
use log::log;
use sqlx::mysql::{MySqlConnectOptions, MySqlPoolOptions, MySqlRow, MySqlSslMode};
use sqlx::{Column, MySqlPool, Row};
use std::str::FromStr;
use uuid::Uuid;

pub struct MySqlDriver {
    pool: MySqlPool,
}

impl MySqlDriver {
    pub fn new(url: &str) -> Self {
        let mut options = MySqlConnectOptions::from_str(url).expect("Invalid MySQL URL");

        options = options.ssl_mode(MySqlSslMode::Disabled);

        let pool = MySqlPool::connect_lazy_with(options);

        Self { pool }
    }
}

#[async_trait]
impl SqlDriverPort for MySqlDriver {
    async fn execute_query(&self, sql: &str) -> AppResult<SqlResult> {
        log::info!("Executing query: {}", sql);

        // We need to check if the query returns rows or not.
        let describe = sqlx::query(sql)
            .describe(&self.pool)
            .await
            .map_err(|e| AppError::BadRequest(format!("Error describing query: {}", e)))?;

        let returns_rows = !describe.columns.is_empty();

        // If the query returns rows, we need to fetch them.
        if returns_rows {
            let rows = sqlx::query(sql)
                .fetch_all(&self.pool)
                .await
                .map_err(|e| AppError::BadRequest(format!("Error executing query: {}", e)))?;

            if rows.is_empty() {
                return Ok(SqlResult {
                    columns: vec![],
                    rows: vec![],
                    affected_rows: 0,
                });
            }

            let columns: Vec<String> = rows[0]
                .columns()
                .iter()
                .map(|c| c.name().to_string())
                .collect();

            let mut row_data: Vec<Vec<String>> = Vec::with_capacity(rows.len());

            for row in &rows {
                let mysql_row: &MySqlRow = row; // make type explicit for cell_to_string
                let mut row_values = Vec::with_capacity(columns.len());

                for i in 0..columns.len() {
                    let value = cell_to_string(mysql_row, i);
                    row_values.push(value);
                }

                row_data.push(row_values);
            }

            Ok(SqlResult {
                columns,
                rows: row_data,
                affected_rows: rows.len() as i64,
            })
        } else {
            // If the query doesn't return rows, we need to execute it.
            let result = sqlx::query(sql)
                .execute(&self.pool)
                .await
                .map_err(|e| AppError::BadRequest(format!("Error executing statement: {}", e)))?;

            let affected = result.rows_affected() as i64;
            let last_id = result.last_insert_id();

            log::info!("Affected rows: {}", affected);
            log::info!("Last insert ID: {}", last_id);

            Ok(SqlResult {
                columns: vec![],
                rows: vec![vec![
                    format!("affected_rows: {}", affected),
                    format!("last_insert_id: {}", last_id),
                ]],
                affected_rows: affected,
            })
        }
    }}

/// Try to render any cell as a String (Handles NULL, Vec<u8>, Uuid, etc.)
fn cell_to_string(row: &MySqlRow, idx: usize) -> String {
    if let Ok(v) = row.try_get::<Option<String>, _>(idx) {
        return if let Some(s) = v {
            s
        } else {
            "NULL".to_string()
        }
    }

    if let Ok(v) = row.try_get::<String, _>(idx) {
        return v;
    }

    if let Ok(v) = row.try_get::<Uuid, _>(idx) {
        return v.to_string();
    }

    if let Ok(bytes) = row.try_get::<Vec<u8>, _>(idx) {
        if bytes.len() == 16 {
            if let Ok(u) = Uuid::from_slice(&bytes) {
                return u.to_string();
            }
        }
        return bytes.iter().map(|b| format!("{:02x}", b)).collect();
    }

    if let Ok(v) = row.try_get::<i64, _>(idx) {
        return v.to_string();
    }
    if let Ok(v) = row.try_get::<i32, _>(idx) {
        return v.to_string();
    }
    if let Ok(v) = row.try_get::<f64, _>(idx) {
        return v.to_string();
    }

    if let Ok(v) = row.try_get::<bool, _>(idx) {
        return if v { "1".to_string() } else { "0".to_string() };
    }

    "<unhandled-type>".to_string()
}
