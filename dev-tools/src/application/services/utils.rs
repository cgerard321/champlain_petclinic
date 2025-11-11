use crate::shared::config::{
    AUTH_SERVICE_DEV_ROLE, BILLING_SERVICE_DEV_ROLE, CART_SERVICE_DEV_ROLE,
    CUSTOMERS_SERVICE_DEV_ROLE, INVENTORY_SERVICE_DEV_ROLE, PRODUCTS_SERVICE_DEV_ROLE,
    VET_SERVICE_DEV_ROLE, VISITS_SERVICE_DEV_ROLE,
};
use crate::shared::error::AppError;
use std::collections::HashMap;
use std::sync::LazyLock;
use uuid::Uuid;

pub fn get_pepper() -> String {
    std::env::var("PASSWORD_PEPPER")
        .map_err(|_| {
            log::error!("PASSWORD_PEPPER env var is not set");
            AppError::Internal
        })
        .expect("Missing password pepper")
}

pub fn resolve_descriptor_by_container(container: &str) -> Option<&'static ServiceDescriptor> {
    let cleaned_name = container.trim();
    log::info!("Resolving descriptor for container: {}", cleaned_name);
    SERVICES.get(cleaned_name)
}

#[derive(Debug)]
pub struct ServiceDescriptor {
    pub docker_service: &'static str,
    pub db: Option<DbDescriptor>,
    pub service_role: Option<Uuid>,
}

#[derive(Debug)]
pub struct DbDescriptor {
    pub db_user_env: &'static str,
    pub db_password_env: &'static str,
    pub db_host: &'static str,
    pub db_name: &'static str,
    pub db_type: DbType,
}

#[derive(Debug)]
pub enum DbType {
    Mongo,
    Sql,
}

impl PartialEq for DbType {
    fn eq(&self, other: &Self) -> bool {
        matches!((self, other), (DbType::Mongo, DbType::Mongo) | (DbType::Sql, DbType::Sql))
    }
}

pub static SERVICES: LazyLock<HashMap<&'static str, ServiceDescriptor>> = LazyLock::new(|| {
    use DbDescriptor as D;
    use DbType::*;
    use ServiceDescriptor as S;

    let mut map = HashMap::new();

    // ======== Frontends (no DBs) ========
    map.insert(
        "petclinic-portal",
        S {
            docker_service: "petclinic-frontend",
            db: None,
            service_role: None,
        },
    );

    map.insert(
        "employee-portal",
        S {
            docker_service: "employee-frontend",
            db: None,
            service_role: None,
        },
    );

    // ======== API Gateway (no DBs) ========
    map.insert(
        "api-gateway",
        S {
            docker_service: "api-gateway",
            db: None,
            service_role: None,
        },
    );

    // ======== Spring Boot (Mongo) ========

    map.insert(
        "visits",
        S {
            docker_service: "visits-service-new",
            db: Some(D {
                db_user_env: "DB_USER",
                db_password_env: "DB_PASSWORD",
                db_host: "mongo-visits",
                db_name: "visits",
                db_type: Mongo,
            }),
            service_role: Some(VISITS_SERVICE_DEV_ROLE),
        },
    );

    map.insert(
        "inventory",
        S {
            docker_service: "inventory-service",
            db: Some(D {
                db_user_env: "DB_USER",
                db_password_env: "DB_PASSWORD",
                db_host: "mongo-inventory",
                db_name: "inventory",
                db_type: Mongo,
            }),
            service_role: Some(INVENTORY_SERVICE_DEV_ROLE),
        },
    );

    map.insert(
        "vet",
        S {
            docker_service: "vet-service",
            db: Some(D {
                db_user_env: "DB_USER",
                db_password_env: "DB_PASSWORD",
                db_host: "mongo-vet",
                db_name: "veterinarians",
                db_type: Mongo,
            }),
            service_role: Some(VET_SERVICE_DEV_ROLE),
        },
    );

    // customers-service-reactive -> mongo-customers
    map.insert(
        "customers",
        S {
            docker_service: "customers-service-reactive",
            db: Some(D {
                db_user_env: "DB_USER",
                db_password_env: "DB_PASSWORD",
                db_host: "mongo-customers",
                db_name: "customers",
                db_type: Mongo,
            }),
            service_role: Some(CUSTOMERS_SERVICE_DEV_ROLE),
        },
    );

    map.insert(
        "billing",
        S {
            docker_service: "billing-service",
            db: Some(D {
                db_user_env: "DB_USER",
                db_password_env: "DB_PASSWORD",
                db_host: "mongo-billing",
                db_name: "billings",
                db_type: Mongo,
            }),
            service_role: Some(BILLING_SERVICE_DEV_ROLE),
        },
    );

    map.insert(
        "products",
        S {
            docker_service: "products-service",
            db: Some(D {
                db_user_env: "DB_USER",
                db_password_env: "DB_PASSWORD",
                db_host: "mongo-products",
                db_name: "products",
                db_type: Mongo,
            }),
            service_role: Some(PRODUCTS_SERVICE_DEV_ROLE),
        },
    );

    map.insert(
        "cart",
        S {
            docker_service: "cart-service",
            db: Some(D {
                db_user_env: "DB_USER",
                db_password_env: "DB_PASSWORD",
                db_host: "mongo-carts",
                db_name: "carts",
                db_type: Mongo,
            }),
            service_role: Some(CART_SERVICE_DEV_ROLE),
        },
    );

    // ======== Spring Boot (MySQL) ========

    map.insert(
        "auth",
        S {
            docker_service: "auth-service",
            db: Some(D {
                db_user_env: "DB_USER",
                db_password_env: "DB_PASSWORD",
                db_host: "mysql-auth",
                db_name: "auth-db",
                db_type: Sql,
            }),
            service_role: Some(AUTH_SERVICE_DEV_ROLE),
        },
    );

    map.insert(
        "files",
        S {
            docker_service: "files-service",
            db: Some(D {
                db_user_env: "DB_USER",
                db_password_env: "DB_PASSWORD",
                db_host: "mysql-files",
                db_name: "files-db",
                db_type: Sql,
            }),
            service_role: None,
        },
    );

    // ======== Go / Utility Services (no DBs) ========

    map.insert(
        "mailer",
        S {
            docker_service: "mailer-service",
            db: None,
            service_role: None,
        },
    );

    map
});
