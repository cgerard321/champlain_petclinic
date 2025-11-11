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
        log::info!("Pool: {:?}", self.pool.connect_options());
        log::info!("Pool: {:?}", self.pool);
        let rows = sqlx::query(sql)
            .fetch_all(&self.pool)
            .await
            .map_err(|e| AppError::BadRequest(format!("Error executing query: {}", e)))?;

        if rows.is_empty() {
            return Ok(SqlResult {
                columns: vec![],
                rows: vec![],
            });
        }

        let columns: Vec<String> = rows[0]
            .columns()
            .iter()
            .map(|c| c.name().to_string())
            .collect();

        let mut row_data: Vec<Vec<String>> = vec![];

        for row in &rows {
            let mut row_values = Vec::new();

            for (i, _) in row.columns().iter().enumerate() {
                let value = cell_to_string(row, i);
                row_values.push(value);
            }

            row_data.push(row_values);
        }

        log::info!("Columns: {:?}", columns);
        Ok(SqlResult {
            columns,
            rows: row_data,
        })
    }
}

/// Try to render any cell as a String (Handles NULL, Vec<u8>, Uuid, etc.)
fn cell_to_string(row: &MySqlRow, idx: usize) -> String {
    // 1) NULL (works for any type)
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
