use sqlx::FromRow;
use uuid::fmt::Hyphenated;

#[derive(FromRow)]

pub struct Service {
    pub docker_service: String,
    pub service_role: Option<Hyphenated>,
}

#[derive(FromRow,Debug)]
pub struct ServiceDb {
    pub db_name: String,
    pub db_user_env: String,
    pub db_password_env: String,
    pub db_host: String,
    pub db_type: String,
}
