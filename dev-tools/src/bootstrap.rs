use crate::adapters::output::minio::client::MinioStore;
use crate::adapters::output::mysql::auth_repo::MySqlAuthRepo;
use crate::adapters::output::mysql::users_repo::MySqlUsersRepo;
use crate::application::ports::input::auth_port::DynAuthPort;
use crate::application::ports::input::files_port::DynFilesPort;
use crate::application::ports::input::user_port::DynUsersPort;
use crate::application::ports::output::auth_repo_port::DynAuthRepo;
use crate::application::ports::output::file_storage_port::DynFileStorage;
use crate::application::ports::output::user_repo_port::DynUsersRepo;
use crate::application::services::auth::service::AuthService;
use crate::application::services::files::service::FilesService;
use crate::application::services::users::params::UserCreationParams;
use crate::application::services::users::service::UsersService;
use crate::core::config::{ADMIN_ROLE_UUID, EDITOR_ROLE_UUID, READER_ROLE_UUID, SUDO_ROLE_UUID};
use rocket::fairing::AdHoc;
use sqlx::mysql::MySqlPoolOptions;
use sqlx::MySqlPool;
use std::collections::HashSet;
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
                eprintln!("Fatal MinIO init error: {e}");
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

        rocket
            .manage(auth_port)
            .manage(files_port)
            .manage(users_port)
    })
}

async fn add_default_roles(pool: &MySqlPool) -> Result<(), sqlx::Error> {
    // TODO : Add role repo
    // Since I don't see a foreseeable future of having a role repo
    // I'm just going to hardcode the roles here.
    let sudo = SUDO_ROLE_UUID.hyphenated().to_string();
    let admin = ADMIN_ROLE_UUID.hyphenated().to_string();
    let reader = READER_ROLE_UUID.hyphenated().to_string();
    let editor = EDITOR_ROLE_UUID.hyphenated().to_string();

    sqlx::query(
        r#"
        INSERT IGNORE INTO roles (id, code, description)
        VALUES (?, 'SUDO',   'Super user'),
               (?, 'ADMIN',  'Administrator'),
               (?, 'READER', 'Read-only'),
               (?, 'EDITOR', 'Editor');
        "#,
    )
    .bind(&sudo)
    .bind(&admin)
    .bind(&reader)
    .bind(&editor)
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

    user_port
        .create_user(params)
        .await
        .expect("Failed to create default admin user");

    Ok(())
}
