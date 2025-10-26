use crate::adapters::output::minio::store::MinioStore;
use crate::adapters::output::mysql::auth_repo::MySqlAuthRepo;
use crate::adapters::output::mysql::users_repo::MySqlUsersRepo;
use crate::application::ports::output::auth_repo_port::DynAuthRepo;
use crate::application::ports::output::file_storage_port::DynFileStorage;
use crate::application::ports::output::user_repo_port::DynUsersRepo;
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

        rocket
            .manage(dyn_auth_repo)
            .manage(storage)
            .manage(dyn_user_repo)
    })
}
