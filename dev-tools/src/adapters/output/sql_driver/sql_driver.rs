use crate::application::ports::output::db_drivers::sql_driver::SqlDriverPort;
use crate::application::services::db_consoles::projections::SqlResult;
use crate::shared::error::{AppError, AppResult};
use async_trait::async_trait;
use log::log;
use sqlx::mysql::{MySqlConnectOptions, MySqlPoolOptions, MySqlSslMode};
use sqlx::{Column, MySqlPool, Row};
use std::str::FromStr;

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

        let mut data: Vec<String> = vec![];
        let mut row_data: Vec<Vec<String>> = vec![];

        for row in &rows {
            log::info!("Row: {:?}", row);
            for (i, col) in row.columns().iter().enumerate() {
                log::info!("Column: {:?}", col);
                let value = row.try_get_raw(i).map_err(|e| {
                    log::info!("Error getting value: {}", e);
                    AppError::BadRequest(format!("Error getting value: {}", e))
                });
                log::info!("Value: {:?}", value.is_ok());

            }
        }

        log::info!("Columns: {:?}", columns);
        Ok(SqlResult {
            columns,
            rows: row_data,
        })
    }
}
