use crate::adapters::input::http::graphql::contracts::sql::SqlResultResponseContract;
use crate::application::services::db_consoles::projections::SqlResult;

impl From<SqlResult> for SqlResultResponseContract {
    fn from(sql_result: SqlResult) -> Self {
        SqlResultResponseContract {
            columns: sql_result.columns,
            rows: sql_result.rows,
            affected_rows: sql_result.affected_rows,
        }
    }
}