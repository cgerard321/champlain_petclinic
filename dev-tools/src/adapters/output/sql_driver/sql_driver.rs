use async_trait::async_trait;
use sqlx::{Column, MySqlPool, Row};
use sqlx::mysql::MySqlPoolOptions;
use crate::application::ports::output::db_drivers::sql_driver::{DynSqlDriver, SqlDriverPort};
use crate::application::services::db_consoles::projections::SqlResult;
use crate::shared::error::{AppError, AppResult};

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
        let rows = sqlx::query(sql)
            .fetch_all(&self.pool)
            .await
            .map_err(AppError::Internal)?;

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

        let data = rows
            .into_iter()
            .map(|row| {
                (0..columns.len())
                    .map(|i| {
                        row.try_get_raw(i)
                            .ok()
                            .map(|v| v.to_string())
                            .unwrap_or_default()
                    })
                    .collect()
            })
            .collect();

        Ok(SqlResult {
            columns,
            rows: data,
        })
    }
}
