use crate::application::ports::output::db_drivers_port::sql_driver::SqlDriverPort;
use crate::application::services::db_consoles::projections::SqlResult;
use crate::shared::error::{AppError, AppResult};
use async_trait::async_trait;
use futures::StreamExt;
use sqlx::postgres::{PgConnectOptions, PgPool, PgRow, PgSslMode};
use sqlx::{Column, Either, Row};
use std::str::FromStr;
use uuid::Uuid;

pub struct PostgresDriver {
    pool: PgPool,
}

impl PostgresDriver {
    pub fn new(url: &str) -> Self {
        let options = PgConnectOptions::from_str(url)
            .expect("Invalid Postgres URL")
            .ssl_mode(PgSslMode::Disable); // same behavior as MySQL version

        let pool = PgPool::connect_lazy_with(options);
        Self { pool }
    }
}

#[async_trait]
impl SqlDriverPort for PostgresDriver {
    async fn execute_query(&self, sql: &str) -> AppResult<SqlResult> {
        log::info!("Executing query (Postgres): {}", sql);

        let mut stream = sqlx::raw_sql(sql).fetch_many(&self.pool);

        let mut total_affected: i64 = 0;
        let mut columns: Vec<String> = Vec::new();
        let mut rows: Vec<Vec<String>> = Vec::new();
        let mut has_columns = false;

        while let Some(item) = stream.next().await {
            let either =
                item.map_err(|e| AppError::BadRequest(format!("Error executing query: {}", e)))?;

            match either {
                Either::Left(res) => {
                    let affected = res.rows_affected() as i64;
                    total_affected += affected;
                    log::info!(
                        "[SQL/Postgres] affected {affected} rows (running total {total_affected})"
                    );
                }

                Either::Right(row) => {
                    let pg_row: PgRow = row;

                    if !has_columns {
                        columns = pg_row
                            .columns()
                            .iter()
                            .map(|c| c.name().to_string())
                            .collect();
                        has_columns = true;
                    }

                    let mut row_values = Vec::with_capacity(columns.len());
                    for i in 0..columns.len() {
                        row_values.push(cell_to_string(&pg_row, i));
                    }
                    rows.push(row_values);
                }
            }
        }

        Ok(SqlResult {
            columns,
            rows,
            affected_rows: total_affected,
        })
    }
}

/// Render any cell in a row as String (handles NULL, UUID, Vec<u8>, numbers, bool)
fn cell_to_string(row: &PgRow, idx: usize) -> String {
    if let Ok(v) = row.try_get::<Option<String>, _>(idx) {
        return v.unwrap_or_else(|| "NULL".to_string());
    }

    if let Ok(v) = row.try_get::<String, _>(idx) {
        return v;
    }

    if let Ok(v) = row.try_get::<Uuid, _>(idx) {
        return v.to_string();
    }

    if let Ok(bytes) = row.try_get::<Vec<u8>, _>(idx) {
        // try to interpret as UUID if 16 bytes
        if bytes.len() == 16 && let Ok(u) = Uuid::from_slice(&bytes) {
            return u.to_string();
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
        return if v { "1".into() } else { "0".into() };
    }

    "<unhandled-type>".to_string()
}
