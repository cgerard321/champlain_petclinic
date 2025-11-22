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
        let db = if let Some(db_name) = db_name {
            self.dbs
                .as_ref()
                .unwrap()
                .iter()
                .find(|db| db.db_name == db_name)
                .ok_or_else(|| {
                    AppError::BadRequest(format!(
                        "Service '{}' has no associated database named '{}'",
                        self.docker_service, db_name
                    ))
                })?
        } else {
            self.dbs.as_ref().unwrap().first().ok_or_else(|| {
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
            DbType::Mongo => "Mongo".to_string(),
            DbType::MySQL => "MySQL".to_string(),
            DbType::Postgres => "Postgres".to_string(),
            DbType::Unknown => "unknown".to_string(),
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
        )
    }
}
