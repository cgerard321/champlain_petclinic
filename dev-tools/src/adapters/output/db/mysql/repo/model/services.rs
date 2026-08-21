use sqlx::FromRow;
use uuid::fmt::Hyphenated;

#[derive(FromRow, Debug)]
pub struct ServiceDb {
    pub db_name: String,
    pub db_user_env: String,
    pub db_password_env: String,
    pub db_host: String,
    pub db_type: String,
}


// This is for joins with service_dbs table
#[derive(FromRow, Debug)]
pub struct ServiceWithDb {
    pub docker_service: String,
    pub service_role: Option<Hyphenated>,

    pub db_name: Option<String>,
    pub db_user_env: Option<String>,
    pub db_password_env: Option<String>,
    pub db_host: Option<String>,
    pub db_type: Option<String>,
}