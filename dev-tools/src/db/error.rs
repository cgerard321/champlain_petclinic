use crate::core::error::AppError;

/// Maps sqlx errors to AppErrors
/// <p>The context is what entity was being operated on (e.g. User) when the error occurred.<p>
pub fn map_sqlx_err(e: sqlx::Error, context: &str) -> AppError {
    match e {
        sqlx::Error::RowNotFound => AppError::NotFound(format!("{context} not found")),
        sqlx::Error::Database(ref db_err)
        if db_err.kind() == sqlx::error::ErrorKind::UniqueViolation =>
            {
                AppError::Conflict
            }
        sqlx::Error::ColumnDecode {
            index: _,
            source: _,
        } => AppError::UnprocessableEntity(format!(
            "{} data was not in the expected format",
            context
        )),
        _ => AppError::UnprocessableEntity(format!("{context} failed")),
    }
}
