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
        .map_err(AppError::Internal)
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
    pub logs_role: Option<Uuid>,
    pub restart_role: Option<Uuid>,
}

#[derive(Debug)]
pub struct DbDescriptor {
    pub db_user_env: &'static str,
    pub db_password_env: &'static str,
    pub db_host: &'static str,
    pub db_type: DbType,
}

#[derive(Debug)]
pub enum DbType {
    Mongo,
    MySql,
}

pub static SERVICES: LazyLock<HashMap<&'static str, ServiceDescriptor>> = LazyLock::new(|| {
    use DbDescriptor as D;
    use DbType::*;
    use ServiceDescriptor as S;

    let mut map = HashMap::new();

    // ======== Frontends (no DBs) ========
    map.insert(
        "petclinic-portal",
        ServiceDescriptor {
            docker_service: "petclinic-frontend",
            db: None,
            logs_role: None,
            restart_role: None,
        },
    );
    map.insert(
        "employee-portal",
        ServiceDescriptor {
            docker_service: "employee-frontend",
            db: None,
            logs_role: None,
            restart_role: None,
        },
    );

    // ======== API Gateway (no DBs) ========
    map.insert(
        "api-gateway",
        ServiceDescriptor {
            docker_service: "api-gateway",
            db: None,
            logs_role: None,
            restart_role: None,
        },
    );

    // ======== Spring Boot (Mongo) ========
    map.insert(
        "visits",
        ServiceDescriptor {
            docker_service: "visits-service-new",
            db: Some(D {
                db_user_env: "DB_USER",
                db_password_env: "DB_PASSWORD",
                db_host: "mongo-visits",
                db_type: Mongo,
            }),
            logs_role: Some(VISITS_SERVICE_DEV_ROLE),
            restart_role: Some(VISITS_SERVICE_DEV_ROLE),
        },
    );

    map.insert(
        "inventory",
        ServiceDescriptor {
            docker_service: "inventory-service",
            db: Some(D {
                db_user_env: "DB_USER",
                db_password_env: "DB_PASSWORD",
                db_host: "mongo-inventory",
                db_type: Mongo,
            }),
            logs_role: Some(INVENTORY_SERVICE_DEV_ROLE),
            restart_role: Some(INVENTORY_SERVICE_DEV_ROLE),
        },
    );

    map.insert(
        "vet",
        ServiceDescriptor {
            docker_service: "vet-service",
            db: Some(D {
                db_user_env: "DB_USER",
                db_password_env: "DB_PASSWORD",
                db_host: "mongo-vet",
                db_type: Mongo,
            }),
            logs_role: Some(VET_SERVICE_DEV_ROLE),
            restart_role: Some(VET_SERVICE_DEV_ROLE),
        },
    );

    map.insert(
        "customers",
        ServiceDescriptor {
            docker_service: "customers-service-reactive",
            db: Some(D {
                db_user_env: "DB_USER",
                db_password_env: "DB_PASSWORD",
                db_host: "mongo-customers",
                db_type: Mongo,
            }),
            logs_role: Some(CUSTOMERS_SERVICE_DEV_ROLE),
            restart_role: Some(CUSTOMERS_SERVICE_DEV_ROLE),
        },
    );

    map.insert(
        "billing",
        ServiceDescriptor {
            docker_service: "billing-service",
            db: Some(D {
                db_user_env: "DB_USER",
                db_password_env: "DB_PASSWORD",
                db_host: "mongo-billing",
                db_type: Mongo,
            }),
            logs_role: Some(BILLING_SERVICE_DEV_ROLE),
            restart_role: Some(BILLING_SERVICE_DEV_ROLE),
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
                db_type: Mongo,
            }),
            logs_role: Some(PRODUCTS_SERVICE_DEV_ROLE),
            restart_role: Some(PRODUCTS_SERVICE_DEV_ROLE),
        },
    );

    map.insert(
        "cart",
        ServiceDescriptor {
            docker_service: "cart-service",
            db: Some(D {
                db_user_env: "DB_USER",
                db_password_env: "DB_PASSWORD",
                db_host: "mongo-carts",
                db_type: Mongo,
            }),
            logs_role: Some(CART_SERVICE_DEV_ROLE),
            restart_role: Some(CART_SERVICE_DEV_ROLE),
        },
    );

    // ======== Spring Boot (MySQL) ========
    map.insert(
        "auth",
        ServiceDescriptor {
            docker_service: "auth-service",
            db: Some(D {
                db_user_env: "DB_USER",
                db_password_env: "DB_PASSWORD",
                db_host: "mysql-auth",
                db_type: MySql,
            }),
            logs_role: Some(AUTH_SERVICE_DEV_ROLE),
            restart_role: Some(AUTH_SERVICE_DEV_ROLE),
        },
    );

    map.insert(
        "files",
        ServiceDescriptor {
            docker_service: "files-service",
            db: Some(D {
                db_user_env: "DB_USER",
                db_password_env: "DB_PASSWORD",
                db_host: "mysql-files",
                db_type: MySql,
            }),
            logs_role: None,
            restart_role: None,
        },
    );

    // ======== Go / Utility Services (no DBs) ========
    map.insert(
        "mailer",
        ServiceDescriptor {
            docker_service: "mailer-service",
            db: None,
            logs_role: None,
            restart_role: None,
        },
    );

    map
});
