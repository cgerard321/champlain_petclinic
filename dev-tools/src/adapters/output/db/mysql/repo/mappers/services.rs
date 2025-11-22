use crate::adapters::output::db::mysql::repo::model::services::ServiceDb;
use crate::domain::entities::service::{DbType, ServiceDbEntity};

impl From<ServiceDb> for ServiceDbEntity {
    fn from(value: ServiceDb) -> Self {
        log::info!("Converting DB: {:?}", value);
        Self {
            db_name: value.db_name,
            db_user_env: value.db_user_env,
            db_password_env: value.db_password_env,
            db_host: value.db_host,
            db_type: match value.db_type.to_lowercase().as_str() {
                "mongo" => DbType::Mongo,
                "mysql" => DbType::MySQL,
                "postgres" => DbType::Postgres,
                _ => DbType::Unknown,
            },
        }
    }
}
