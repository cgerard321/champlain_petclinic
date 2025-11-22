use crate::shared::error::{AppError, AppResult};
use std::fmt::Display;
use uuid::Uuid;

#[derive(Debug)]
pub struct ServiceEntity {
    pub docker_service: String,
    pub dbs: Option<Vec<ServiceDbEntity>>,
    pub service_role: Option<Uuid>,
}

impl ServiceEntity {
    pub fn get_db_by_name_or_default(
        &self,
        db_name: Option<String>,
    ) -> AppResult<&ServiceDbEntity> {
        let dbs = self.dbs.as_ref().ok_or_else(|| {
            AppError::BadRequest(format!(
                "Service '{}' has no associated database",
                self.docker_service
            ))
        })?;

        let db = if let Some(db_name) = db_name {
            dbs.iter().find(|db| db.db_name == db_name).ok_or_else(|| {
                AppError::BadRequest(format!(
                    "Service '{}' has no associated database named '{}'",
                    self.docker_service, db_name
                ))
            })?
        } else {
            dbs.first().ok_or_else(|| {
                AppError::BadRequest(format!(
                    "Service '{}' has no associated database",
                    self.docker_service
                ))
            })?
        };

        Ok(db)
    }
}
#[derive(Debug)]
pub struct ServiceDbEntity {
    pub db_name: String,
    pub db_user_env: String,
    pub db_password_env: String,
    pub db_host: String,
    pub db_type: DbType,
}

#[derive(Debug)]
pub enum DbType {
    Mongo,
    MySQL,
    Postgres,
    Unknown,
}

impl Display for DbType {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        let str = match self {
            DbType::Mongo => "Mongo",
            DbType::MySQL => "MySQL",
            DbType::Postgres => "Postgres",
            DbType::Unknown => "unknown",
        };
        write!(f, "{}", str)
    }
}

impl PartialEq for DbType {
    fn eq(&self, other: &Self) -> bool {
        matches!(
            (self, other),
            (DbType::Mongo, DbType::Mongo)
                | (DbType::MySQL, DbType::MySQL)
                | (DbType::Postgres, DbType::Postgres)
                | (DbType::Unknown, DbType::Unknown)
        )
    }
}
