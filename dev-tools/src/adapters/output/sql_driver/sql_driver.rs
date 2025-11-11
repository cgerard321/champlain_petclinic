use crate::application::ports::output::db_drivers::sql_driver::SqlDriverPort;
use crate::application::services::db_consoles::projections::SqlResult;
use crate::shared::error::{AppError, AppResult};
use async_trait::async_trait;
use log::log;
use sqlx::mysql::MySqlPoolOptions;
use sqlx::{Column, MySqlPool, Row};

pub struct MySqlDriver {
    pool: MySqlPool,
}

impl MySqlDriver {
    pub fn new(url: &str) -> Self {
        let pool = MySqlPoolOptions::new()
            .max_connections(5)
            .connect_lazy(url)
            .expect("Invalid MySQL URL");
        Self { pool }
    }
}

#[async_trait]
impl SqlDriverPort for MySqlDriver {
    async fn execute_query(&self, sql: &str) -> AppResult<SqlResult> {
        log::info!("Executing query: {}", sql);
        let rows = sqlx::query(sql)
            .fetch_all(&self.pool)
            .await
            .map_err(|_| AppError::Internal)?;

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

        log::info!("Columns: {:?}", columns);
        Ok(SqlResult {
            columns,
            rows: Default::default(),
        })
    }
}
