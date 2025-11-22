use crate::adapters::output::db::mysql::repo::error::map_sqlx_err;
use crate::adapters::output::db::mysql::repo::model::services::ServiceWithDb;
use crate::application::ports::output::services_repo_port::ServicesRepoPort;
use crate::domain::entities::service::{DbType, ServiceDbEntity, ServiceEntity};
use crate::shared::error::{AppError, AppResult};

use sqlx::{MySql, Pool};
use std::collections::HashMap;
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
        let rows = sqlx::query_as::<_, ServiceWithDb>(
            r#"
            SELECT
                s.docker_service,
                s.service_role,
                d.db_name,
                d.db_user_env,
                d.db_password_env,
                d.db_host,
                d.db_type
            FROM services s
            LEFT JOIN service_dbs d
                ON s.docker_service = d.service_docker_service
            ORDER BY s.docker_service
            "#,
        )
        .fetch_all(&*self.pool)
        .await
        .map_err(|e| map_sqlx_err(e, "Services"))?;

        let mut map: HashMap<String, (Option<Hyphenated>, Vec<ServiceDbEntity>)> = HashMap::new();

        for row in rows {
            let entry = map
                .entry(row.docker_service.clone())
                .or_insert((row.service_role, Vec::new()));

            if let (
                Some(db_name),
                Some(db_user_env),
                Some(db_password_env),
                Some(db_host),
                Some(db_type),
            ) = (
                row.db_name,
                row.db_user_env,
                row.db_password_env,
                row.db_host,
                row.db_type,
            ) {
                entry.1.push(ServiceDbEntity {
                    db_name,
                    db_user_env,
                    db_password_env,
                    db_host,
                    db_type: DbType::from(db_type),
                });
            }
        }

        let services = map
            .into_iter()
            .map(|(docker_service, (role, dbs))| {
                Ok(ServiceEntity {
                    docker_service,
                    dbs: if dbs.is_empty() { None } else { Some(dbs) },
                    service_role: convert_service_role(role)?,
                })
            })
            .collect::<Result<Vec<_>, AppError>>()?;

        Ok(services)
    }

    async fn get_service(&self, service_name: &str) -> AppResult<ServiceEntity> {
        let rows = sqlx::query_as::<_, ServiceWithDb>(
            r#"
            SELECT
                s.docker_service,
                s.service_role,
                d.db_name,
                d.db_user_env,
                d.db_password_env,
                d.db_host,
                d.db_type
            FROM services s
            LEFT JOIN service_dbs d
                ON s.docker_service = d.service_docker_service
            WHERE s.docker_service = ?
            "#,
        )
        .bind(service_name)
        .fetch_all(&*self.pool)
        .await
        .map_err(|e| map_sqlx_err(e, "Service"))?;

        if rows.is_empty() {
            return Err(AppError::NotFound(format!(
                "Service '{}' not found",
                service_name
            )));
        }

        let mut dbs = Vec::new();
        let role = rows[0].service_role;

        for row in rows {
            if let (
                Some(db_name),
                Some(db_user_env),
                Some(db_password_env),
                Some(db_host),
                Some(db_type),
            ) = (
                row.db_name,
                row.db_user_env,
                row.db_password_env,
                row.db_host,
                row.db_type,
            ) {
                dbs.push(ServiceDbEntity {
                    db_name,
                    db_user_env,
                    db_password_env,
                    db_host,
                    db_type: DbType::from(db_type),
                });
            }
        }

        Ok(ServiceEntity {
            docker_service: service_name.to_string(),
            dbs: if dbs.is_empty() { None } else { Some(dbs) },
            service_role: convert_service_role(role)?,
        })
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
