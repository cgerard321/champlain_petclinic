use crate::adapters::output::crypto::crypto_functions::CryptoFunctions;
use crate::adapters::output::db::mongo::console::driver::MongoDriver;
use crate::adapters::output::db::mysql::console::driver::MySqlDriver;
use crate::adapters::output::db::mysql::repo::auth_repo::MySqlAuthRepo;
use crate::adapters::output::db::mysql::repo::services_repo::MySqlServicesRepo;
use crate::adapters::output::db::mysql::repo::users_repo::MySqlUsersRepo;
use crate::adapters::output::db::postgres::console::driver::PostgresDriver;
use crate::adapters::output::docker::client::BollardDockerAPI;
use crate::adapters::output::minio::client::MinioStore;
use crate::application::ports::input::auth_port::DynAuthPort;
use crate::application::ports::input::docker_port::DynDockerPort;
use crate::application::ports::input::files_port::DynFilesPort;
use crate::application::ports::input::mongo_console_port::DynMongoConsolePort;
use crate::application::ports::input::sql_console_port::DynSqlConsolePort;
use crate::application::ports::input::user_port::DynUsersPort;
use crate::application::ports::output::auth_repo_port::DynAuthRepo;
use crate::application::ports::output::db_drivers_port::mongo_driver::DynMongoDriver;
use crate::application::ports::output::db_drivers_port::sql_driver::DynSqlDriver;
use crate::application::ports::output::docker_api_port::DynDockerAPI;
use crate::application::ports::output::file_storage_port::DynFileStorage;
use crate::application::ports::output::services_repo_port::DynServicesRepo;
use crate::application::ports::output::user_repo_port::DynUsersRepo;
use crate::application::services::auth::service::AuthService;
use crate::application::services::db_consoles::mongo_service::MongoConsoleService;
use crate::application::services::db_consoles::sql_service::SqlConsoleService;
use crate::application::services::docker::service::DockerService;
use crate::application::services::files::service::FilesService;
use crate::application::services::user_context::UserContext;
use crate::application::services::users::params::UserCreationParams;
use crate::application::services::users::service::UsersService;
use crate::domain::entities::service::DbType;
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

        let service_repo = MySqlServicesRepo::new(Arc::new(pool.clone()));
        let dyn_service_repo: DynServicesRepo = Arc::new(service_repo);

        // MinIO
        let store = MinioStore::from_env()
            .map_err(|e| {
                log::error!("Failed to create MinIO client: {e}");
                e
            })
            .expect("MinIO config must be valid at startup");

        let storage: DynFileStorage = Arc::new(store);

        let crypto_port = CryptoFunctions::new();
        let crypto = Arc::new(crypto_port);

        // Ports
        let auth_port: DynAuthPort = Arc::new(AuthService::new(
            dyn_auth_repo.clone(),
            dyn_user_repo.clone(),
            crypto.clone(),
        ));
        let files_port: DynFilesPort = Arc::new(FilesService::new(storage.clone()));
        let users_port: DynUsersPort =
            Arc::new(UsersService::new(dyn_user_repo.clone(), crypto.clone()));

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
        let docker_port: DynDockerPort = Arc::new(DockerService::new(
            docker_api.clone(),
            dyn_service_repo.clone(),
        ));

        // Default services
        if let Err(e) = add_default_services(&pool).await {
            log::error!("Failed to insert default services: {e}");
        }

        // SQL Console
        let drivers = build_sql_drivers_from_services(dyn_service_repo.clone()).await;

        let sql_console_port: DynSqlConsolePort =
            Arc::new(SqlConsoleService::new(drivers, dyn_service_repo.clone()));

        // Mongo Console
        let mongo_drivers = build_mongo_drivers_from_services(dyn_service_repo.clone()).await;

        let mongo_console_port: DynMongoConsolePort = Arc::new(MongoConsoleService::new(
            mongo_drivers,
            dyn_service_repo.clone(),
        ));

        rocket
            .manage(auth_port)
            .manage(files_port)
            .manage(users_port)
            .manage(docker_port)
            .manage(sql_console_port)
            .manage(mongo_console_port)
    })
}

pub async fn build_mongo_drivers_from_services(
    dyn_services_repo: DynServicesRepo,
) -> HashMap<String, Arc<DynMongoDriver>> {
    let mut map = HashMap::new();

    let services = match dyn_services_repo.list_services().await {
        Ok(s) => s,
        Err(e) => {
            log::error!("Failed to list services: {e}");
            return map;
        }
    };

    for svc in services {
        let Some(dbs) = &svc.dbs else { continue };

        for db in dbs {
            if !matches!(db.db_type, DbType::Mongo) {
                continue;
            }

            let id = db.db_host.clone();

            if map.contains_key(&id) {
                continue;
            }

            let user = std::env::var(&db.db_user_env)
                .unwrap_or_else(|_| panic!("Missing env {}", db.db_user_env));
            let pass = std::env::var(&db.db_password_env)
                .unwrap_or_else(|_| panic!("Missing env {}", db.db_password_env));

            let uri = format!(
                "mongodb://{}:{}@{}:{}/{}?authSource={}",
                user, pass, db.db_host, 27017, db.db_name, "admin"
            );

            let driver = MongoDriver::new(&uri).await;

            map.insert(id, Arc::new(driver) as Arc<DynMongoDriver>);
        }
    }

    map
}

pub async fn build_sql_drivers_from_services(
    dyn_services_repo: DynServicesRepo,
) -> HashMap<String, Arc<DynSqlDriver>> {
    let mut map: HashMap<String, Arc<DynSqlDriver>> = HashMap::new();

    let services = match dyn_services_repo.list_services().await {
        Ok(s) => s,
        Err(e) => {
            log::error!("Failed to list services: {e}");
            return map;
        }
    };

    for svc in services {
        let Some(dbs) = &svc.dbs else { continue };

        for db in dbs {
            match db.db_type {
                DbType::MySQL | DbType::Postgres => {}
                _ => continue,
            }

            let id = db.db_host.clone();
            if map.contains_key(&id) {
                continue;
            }

            let user = std::env::var(&db.db_user_env)
                .unwrap_or_else(|_| panic!("Missing env {}", db.db_user_env));
            let pass = std::env::var(&db.db_password_env)
                .unwrap_or_else(|_| panic!("Missing env {}", db.db_password_env));

            let driver: Arc<DynSqlDriver> = match db.db_type {
                DbType::MySQL => {
                    let url = format!("mysql://{}:{}@{}/{}", user, pass, db.db_host, db.db_name);
                    Arc::new(MySqlDriver::new(&url)) as Arc<DynSqlDriver>
                }
                DbType::Postgres => {
                    let url = format!("postgres://{}:{}@{}/{}", user, pass, db.db_host, db.db_name);
                    Arc::new(PostgresDriver::new(&url)) as Arc<DynSqlDriver>
                }
                _ => continue,
            };

            map.insert(id, driver);
        }
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

async fn add_default_services(pool: &MySqlPool) -> Result<(), sqlx::Error> {
    use crate::shared::config::{
        AUTH_SERVICE_DEV_ROLE, BILLING_SERVICE_DEV_ROLE, CART_SERVICE_DEV_ROLE,
        CUSTOMERS_SERVICE_DEV_ROLE, INVENTORY_SERVICE_DEV_ROLE, PRODUCTS_SERVICE_DEV_ROLE,
        VET_SERVICE_DEV_ROLE, VISITS_SERVICE_DEV_ROLE,
    };

    let services = vec![
        ("petclinic-frontend", None),
        ("employee-frontend", None),
        ("api-gateway", None),
        ("mailer-service", None),
        ("files-service", None),
        ("visits-service-new", Some(VISITS_SERVICE_DEV_ROLE)),
        ("inventory-service", Some(INVENTORY_SERVICE_DEV_ROLE)),
        ("vet-service", Some(VET_SERVICE_DEV_ROLE)),
        (
            "customers-service-reactive",
            Some(CUSTOMERS_SERVICE_DEV_ROLE),
        ),
        ("billing-service", Some(BILLING_SERVICE_DEV_ROLE)),
        ("products-service", Some(PRODUCTS_SERVICE_DEV_ROLE)),
        ("cart-service", Some(CART_SERVICE_DEV_ROLE)),
        ("auth-service", Some(AUTH_SERVICE_DEV_ROLE)),
    ];

    for (docker_service, role) in services {
        let role = role.map(|r| r.hyphenated().to_string());

        sqlx::query("INSERT IGNORE INTO services (docker_service, service_role) VALUES (?, ?)")
            .bind(docker_service)
            .bind(role)
            .execute(pool)
            .await?;
    }

    let dbs = vec![
        (
            "visits-service-new",
            "visits",
            "DB_USER",
            "DB_PASSWORD",
            "mongo-visits",
            "MONGO",
        ),
        (
            "inventory-service",
            "inventory",
            "DB_USER",
            "DB_PASSWORD",
            "mongo-inventory",
            "MONGO",
        ),
        (
            "vet-service",
            "veterinarians",
            "DB_USER",
            "DB_PASSWORD",
            "mongo-vet",
            "MONGO",
        ),
        (
            "vet-service",
            "images",
            "DB_USER",
            "DB_PASSWORD",
            "postgres-vet",
            "POSTGRES",
        ),
        (
            "customers-service-reactive",
            "customers",
            "DB_USER",
            "DB_PASSWORD",
            "mongo-customers",
            "MONGO",
        ),
        (
            "billing-service",
            "billings",
            "DB_USER",
            "DB_PASSWORD",
            "mongo-billing",
            "MONGO",
        ),
        (
            "products-service",
            "products",
            "DB_USER",
            "DB_PASSWORD",
            "mongo-products",
            "MONGO",
        ),
        (
            "cart-service",
            "carts",
            "DB_USER",
            "DB_PASSWORD",
            "mongo-carts",
            "MONGO",
        ),
        (
            "auth-service",
            "auth-db",
            "DB_USER",
            "DB_PASSWORD",
            "mysql-auth",
            "MYSQL",
        ),
        (
            "files-service",
            "files-db",
            "DB_USER",
            "DB_PASSWORD",
            "mysql-files",
            "MYSQL",
        ),
    ];

    for (service, db_name, user_env, pass_env, host, db_type) in dbs {
        sqlx::query(
            "INSERT IGNORE INTO service_dbs
             (service_docker_service, db_name, db_user_env, db_password_env, db_host, db_type)
             VALUES (?, ?, ?, ?, ?, ?)",
        )
            .bind(service)
            .bind(db_name)
            .bind(user_env)
            .bind(pass_env)
            .bind(host)
            .bind(db_type)
            .execute(pool)
            .await?;
    }

    Ok(())
}
