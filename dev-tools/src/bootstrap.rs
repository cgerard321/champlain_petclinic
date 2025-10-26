use crate::adapters::output::minio::store::MinioStore;
use crate::adapters::output::mysql::auth_repo::MySqlAuthRepo;
use crate::adapters::output::mysql::users_repo::MySqlUsersRepo;
use crate::application::ports::input::auth_port::DynAuthPort;
use crate::application::ports::input::files_port::DynFilesPort;
use crate::application::ports::input::user_port::DynUsersPort;
use crate::application::ports::output::auth_repo_port::DynAuthRepo;
use crate::application::ports::output::file_storage_port::DynFileStorage;
use crate::application::ports::output::user_repo_port::DynUsersRepo;
use crate::application::usecases::auth::service::AuthService;
use crate::application::usecases::files::service::FilesService;
use crate::application::usecases::users::service::UsersService;
use rocket::fairing::AdHoc;
use sqlx::mysql::MySqlPoolOptions;
use std::sync::Arc;

pub fn stage() -> AdHoc {
    AdHoc::on_ignite("SQLx (MySQL)", |rocket| async move {
        let url = std::env::var("DATABASE_URL").expect("Missing DATABASE_URL env var");
        // MySQL
        let pool = MySqlPoolOptions::new()
            .max_connections(10)
            .connect(&url)
            .await
            .expect("DB connect error");

        sqlx::migrate!("./src/migrations")
            .run(&pool)
            .await
            .expect("Migrations failed");

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

        rocket
            .manage(auth_port)
            .manage(files_port)
            .manage(users_port)
    })
}
