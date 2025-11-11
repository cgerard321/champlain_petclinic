use crate::adapters::output::docker::client::BollardDockerAPI;
use crate::adapters::output::minio::client::MinioStore;
use crate::adapters::output::mysql::auth_repo::MySqlAuthRepo;
use crate::adapters::output::mysql::users_repo::MySqlUsersRepo;
use crate::adapters::output::sql_driver::sql_driver::MySqlDriver;
use crate::application::ports::input::auth_port::DynAuthPort;
use crate::application::ports::input::docker_logs_port::DynDockerPort;
use crate::application::ports::input::files_port::DynFilesPort;
use crate::application::ports::input::mongo_console_port::DynMongoConsolePort;
use crate::application::ports::input::sql_console_port::{DynSqlConsolePort, SqlConsolePort};
use crate::application::ports::input::user_port::DynUsersPort;
use crate::application::ports::output::auth_repo_port::DynAuthRepo;
use crate::application::ports::output::db_drivers::sql_driver::DynSqlDriver;
use crate::application::ports::output::docker_api::DynDockerAPI;
use crate::application::ports::output::file_storage_port::DynFileStorage;
use crate::application::ports::output::user_repo_port::DynUsersRepo;
use crate::application::services::auth::service::AuthService;
use crate::application::services::db_consoles::mongo_service::MongoConsoleService;
use crate::application::services::db_consoles::sql_service::SqlConsoleService;
use crate::application::services::docker::service::DockerService;
use crate::application::services::files::service::FilesService;
use crate::application::services::user_context::UserContext;
use crate::application::services::users::params::UserCreationParams;
use crate::application::services::users::service::UsersService;
use crate::application::services::{DbType, SERVICES};
use crate::shared::config::{
    ADMIN_ROLE_UUID, AUTH_SERVICE_DEV_ROLE, BILLING_SERVICE_DEV_ROLE, CART_SERVICE_DEV_ROLE,
    CUSTOMERS_SERVICE_DEV_ROLE, EDITOR_ROLE_UUID, INVENTORY_SERVICE_DEV_ROLE,
    PRODUCTS_SERVICE_DEV_ROLE, READER_ROLE_UUID, SUDO_ROLE_UUID, VET_SERVICE_DEV_ROLE,
    VISITS_SERVICE_DEV_ROLE,
};
use bollard::Docker;
use rocket::fairing::AdHoc;
use sqlx::mysql::MySqlPoolOptions;
use sqlx::MySqlPool;
use std::collections::{HashMap, HashSet};
use std::sync::Arc;

pub fn stage() -> AdHoc {
    AdHoc::on_ignite("SQLx (MySQL)", |rocket| async move {
        let url = std::env::var("DATABASE_URL").expect("Missing DATABASE_URL env var");
        // MySQL
        log::info!("Connecting to MySQL");
        let pool = MySqlPoolOptions::new()
            .max_connections(10)
            .connect(&url)
            .await
            .expect("DB connect error");

        log::info!("Connected to MySQL, running migration script");

        sqlx::migrate!("./src/migrations")
            .run(&pool)
            .await
            .expect("Migrations failed");

        log::info!("Migration script ran successfully");

        if let Err(e) = add_default_roles(&pool).await {
            log::error!("Failed to insert default roles: {e}");
        }

        let auth_repo = MySqlAuthRepo::new(Arc::new(pool.clone()));
        let dyn_auth_repo: DynAuthRepo = Arc::new(auth_repo);

        let user_repo = MySqlUsersRepo::new(Arc::new(pool.clone()));
        let dyn_user_repo: DynUsersRepo = Arc::new(user_repo);

        // MinIO
        let store = MinioStore::from_env()
            .map_err(|e| {
                log::error!("Failed to create MinIO client: {e}");
                e
            })
            .expect("MinIO config must be valid at startup");

        let storage: DynFileStorage = Arc::new(store);

        // Ports
        let auth_port: DynAuthPort = Arc::new(AuthService::new(
            dyn_auth_repo.clone(),
            dyn_user_repo.clone(),
        ));
        let files_port: DynFilesPort = Arc::new(FilesService::new(storage.clone()));
        let users_port: DynUsersPort = Arc::new(UsersService::new(dyn_user_repo.clone()));

        if let Err(e) = add_default_user(&users_port, &pool).await {
            log::error!("Failed to insert default user: {e}");
        }

        // Docker
        let docker = Docker::connect_with_unix_defaults()
            .map_err(|e| {
                log::error!("Failed to create Docker client: {e}");
                e
            })
            .expect("Docker must be available at startup");

        let docker_api: DynDockerAPI = Arc::new(BollardDockerAPI::new(docker));
        let docker_port: DynDockerPort = Arc::new(DockerService::new(docker_api.clone()));

        // SQL Console
        let drivers = build_sql_drivers_from_services();

        let sql_console_port: DynSqlConsolePort = Arc::new(SqlConsoleService::new(drivers));

        // Mongo Console
        let mongo_console_port: DynMongoConsolePort = Arc::new(MongoConsoleService::new());

        rocket
            .manage(auth_port)
            .manage(files_port)
            .manage(users_port)
            .manage(docker_port)
            .manage(sql_console_port)
            .manage(mongo_console_port)
    })
}

pub fn build_sql_drivers_from_services() -> HashMap<&'static str, Arc<DynSqlDriver>> {
    let mut map = HashMap::new();

    for (_name, svc) in SERVICES.iter() {
        let Some(db) = &svc.db else { continue };

        if !matches!(db.db_type, DbType::Sql) {
            continue;
        }

        let id = db.db_host;
        if map.contains_key(id) {
            continue;
        }

        let user = std::env::var(db.db_user_env)
            .unwrap_or_else(|_| panic!("Missing env {}", db.db_user_env));
        let pass = std::env::var(db.db_password_env)
            .unwrap_or_else(|_| panic!("Missing env {}", db.db_password_env));

        let url = format!(
            "mysql://{}:{}@{}:3306/{}",
            user, pass, db.db_host, db.db_name
        );

        let driver = MySqlDriver::new(&url);
        map.insert(id, Arc::new(driver) as Arc<DynSqlDriver>);
    }

    map
}

async fn add_default_roles(pool: &MySqlPool) -> Result<(), sqlx::Error> {
    // TODO : Add role repo
    // Since I don't see a foreseeable future of having a role repo
    // I'm just going to hardcode the roles here.
    let sudo = SUDO_ROLE_UUID.hyphenated().to_string();
    let admin = ADMIN_ROLE_UUID.hyphenated().to_string();
    let reader = READER_ROLE_UUID.hyphenated().to_string();
    let editor = EDITOR_ROLE_UUID.hyphenated().to_string();
    let auth_service_dev = AUTH_SERVICE_DEV_ROLE.hyphenated().to_string();
    let vet_service_dev = VET_SERVICE_DEV_ROLE.hyphenated().to_string();
    let visits_service_dev = VISITS_SERVICE_DEV_ROLE.hyphenated().to_string();
    let customers_service_dev = CUSTOMERS_SERVICE_DEV_ROLE.hyphenated().to_string();
    let products_service_dev = PRODUCTS_SERVICE_DEV_ROLE.hyphenated().to_string();
    let cart_service_dev = CART_SERVICE_DEV_ROLE.hyphenated().to_string();
    let inventory_service_dev = INVENTORY_SERVICE_DEV_ROLE.hyphenated().to_string();
    let billing_service_dev = BILLING_SERVICE_DEV_ROLE.hyphenated().to_string();

    sqlx::query(
        r#"
    INSERT IGNORE INTO roles (id, code, description)
    VALUES 
        (?, 'SUDO',                   'Super user'),
        (?, 'ADMIN',                  'Administrator'),
        (?, 'READER',                 'Read-only'),
        (?, 'EDITOR',                 'Editor'),
        (?, 'AUTH_SERVICE_DEV',       'Auth Service Dev'),
        (?, 'VET_SERVICE_DEV',        'Vet Service Dev'),
        (?, 'VISITS_SERVICE_DEV',     'Visits Service Dev'),
        (?, 'CUSTOMERS_SERVICE_DEV',  'Customers Service Dev'),
        (?, 'PRODUCTS_SERVICE_DEV',   'Products Service Dev'),
        (?, 'CART_SERVICE_DEV',       'Cart Service Dev'),
        (?, 'INVENTORY_SERVICE_DEV',  'Inventory Service Dev'),
        (?, 'BILLING_SERVICE_DEV',    'Billing Service Dev');
    "#,
    )
    .bind(&sudo)
    .bind(&admin)
    .bind(&reader)
    .bind(&editor)
    .bind(&auth_service_dev)
    .bind(&vet_service_dev)
    .bind(&visits_service_dev)
    .bind(&customers_service_dev)
    .bind(&products_service_dev)
    .bind(&cart_service_dev)
    .bind(&inventory_service_dev)
    .bind(&billing_service_dev)
    .execute(pool)
    .await?;
    Ok(())
}

async fn add_default_user(user_port: &DynUsersPort, pool: &MySqlPool) -> Result<(), sqlx::Error> {
    let admin_email =
        std::env::var("DEFAULT_ADMIN_EMAIL").expect("Missing DEFAULT_ADMIN_EMAIL env var");

    // Check if default user exists
    let row = sqlx::query("SELECT * FROM users WHERE email = ?")
        .bind(&admin_email)
        .fetch_optional(pool)
        .await?;

    if row.is_some() {
        return Ok(());
    }

    let admin_password =
        std::env::var("DEFAULT_ADMIN_PASSWORD").expect("Missing DEFAULT_ADMIN_PASSWORD env var");

    let admin_roles = HashSet::from([
        SUDO_ROLE_UUID,
        ADMIN_ROLE_UUID,
        READER_ROLE_UUID,
        EDITOR_ROLE_UUID,
    ]);

    let params = UserCreationParams {
        email: admin_email,
        password: admin_password,
        display_name: "Administrator".to_string(),
        roles: admin_roles,
    };

    let auth_context = UserContext::system();

    user_port
        .create_user(params, auth_context)
        .await
        .expect("Failed to create default user");

    Ok(())
}
