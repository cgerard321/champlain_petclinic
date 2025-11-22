use crate::adapters::output::db::mysql::repo::error::map_sqlx_err;
use crate::adapters::output::db::mysql::repo::model::services::{Service, ServiceDb};
use crate::application::ports::output::services_repo_port::ServicesRepoPort;
use crate::domain::entities::service::{ServiceDbEntity, ServiceEntity};
use crate::shared::error::{AppError, AppResult};
use sqlx::{MySql, Pool};
use std::sync::Arc;
use uuid::fmt::Hyphenated;
use uuid::Uuid;

pub struct MySqlServicesRepo {
    pool: Arc<Pool<MySql>>,
}

impl MySqlServicesRepo {
    pub fn new(pool: Arc<Pool<MySql>>) -> Self {
        Self { pool }
    }
}

#[async_trait::async_trait]
impl ServicesRepoPort for MySqlServicesRepo {
    async fn list_services(&self) -> AppResult<Vec<ServiceEntity>> {
        let services =
            sqlx::query_as::<_, Service>("SELECT docker_service, service_role FROM services")
                .fetch_all(&*self.pool)
                .await
                .map_err(|e| map_sqlx_err(e, "Services"))?;

        let mut entities = Vec::with_capacity(services.len());

        for s in services {
            let dbs = self.get_service_dbs(&s.docker_service).await?;

            entities.push(ServiceEntity {
                docker_service: s.docker_service,
                dbs,
                service_role: convert_service_role(s.service_role)?,
            });
        }

        Ok(entities)
    }

    async fn get_service(&self, service_name: &str) -> AppResult<ServiceEntity> {
        let s = sqlx::query_as::<_, Service>(
            "SELECT docker_service, service_role FROM services WHERE docker_service = ?",
        )
            .bind(service_name)
            .fetch_one(&*self.pool)
            .await
            .map_err(|e| map_sqlx_err(e, "Service"))?;

        let dbs = self.get_service_dbs(service_name).await?;

        Ok(ServiceEntity {
            docker_service: s.docker_service,
            dbs,
            service_role: convert_service_role(s.service_role)?,
        })
    }

    async fn get_service_dbs(&self, service_name: &str) -> AppResult<Option<Vec<ServiceDbEntity>>> {
        let db_rows = sqlx::query_as::<_, ServiceDb>(
            "SELECT db_name, db_user_env, db_password_env, db_host, db_type
             FROM service_dbs
             WHERE service_docker_service = ?",
        )
            .bind(service_name)
            .fetch_all(&*self.pool)
            .await
            .map_err(|e| map_sqlx_err(e, "Service DB"))?;

        let dbs = if db_rows.is_empty() {
            None
        } else {
            Some(db_rows.into_iter().map(ServiceDbEntity::from).collect())
        };

        log::info!("Service '{}' has DBs: {:?}", service_name, dbs);

        Ok(dbs)
    }

    async fn add_service(&self, service: &ServiceEntity) -> AppResult<()> {
        use crate::domain::entities::service::DbType;

        let mut tx = self
            .pool
            .begin()
            .await
            .map_err(|e| map_sqlx_err(e, "Service"))?;

        let service_role: Option<String> = service.service_role.map(|id| id.to_string());

        sqlx::query("INSERT INTO services (docker_service, service_role) VALUES (?, ?)")
            .bind(&service.docker_service)
            .bind(service_role)
            .execute(&mut *tx)
            .await
            .map_err(|e| map_sqlx_err(e, "Service"))?;

        if let Some(ref dbs) = service.dbs {
            for db in dbs {
                let db_type_str = match db.db_type {
                    DbType::Mongo => "MONGO",
                    DbType::MySQL => "MYSQL",
                    DbType::Postgres => "POSTGRES",
                    DbType::Unknown => "UNKNOWN",
                };

                sqlx::query(
                    "INSERT INTO service_dbs \
                     (service_docker_service, db_name, db_user_env, db_password_env, db_host, db_type) \
                     VALUES (?, ?, ?, ?, ?, ?)",
                )
                    .bind(&service.docker_service)
                    .bind(&db.db_name)
                    .bind(&db.db_user_env)
                    .bind(&db.db_password_env)
                    .bind(&db.db_host)
                    .bind(db_type_str)
                    .execute(&mut *tx)
                    .await
                    .map_err(|e| map_sqlx_err(e, "Service DB"))?;
            }
        }

        tx.commit().await.map_err(|e| map_sqlx_err(e, "Service"))?;

        Ok(())
    }
}

fn convert_service_role(h: Option<Hyphenated>) -> AppResult<Option<Uuid>> {
    match h {
        Some(hyp) => {
            let uuid = Uuid::parse_str(&hyp.to_string()).map_err(|_| AppError::Internal)?;
            Ok(Some(uuid))
        }
        None => Ok(None),
    }
}
