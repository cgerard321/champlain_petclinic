use rocket::fairing::AdHoc;
use sqlx::mysql::MySqlPoolOptions;
use sqlx::{MySql, Pool};

pub type MyPool = Pool<MySql>;
pub struct Db(pub MyPool);

pub fn stage() -> AdHoc {
    AdHoc::on_ignite("SQLx (MySQL)", |rocket| async move {
        let url = std::env::var("DATABASE_URL").expect("Missing DATABASE_URL env var");

        let pool = MySqlPoolOptions::new()
            .max_connections(10)
            .connect(&url)
            .await
            .expect("DB connect error");

        sqlx::migrate!("./src/migrations")
            .run(&pool)
            .await
            .expect("Migrations failed");

        rocket.manage(Db(pool))
    })
}
