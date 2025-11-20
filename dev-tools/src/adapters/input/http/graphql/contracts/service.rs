pub struct ServiceResponseContract {
    pub name: String,
    pub docker_service: String,
    pub db_name: Option<String>,
    pub db_host: Option<String>,
    pub db_type: Option<String>,
}
