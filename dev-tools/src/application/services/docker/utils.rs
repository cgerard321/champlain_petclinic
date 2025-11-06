use crate::shared::config::{
    AUTH_SERVICE_DEV_ROLE, BILLING_SERVICE_DEV_ROLE, CART_SERVICE_DEV_ROLE,
    CUSTOMERS_SERVICE_DEV_ROLE, INVENTORY_SERVICE_DEV_ROLE, PRODUCTS_SERVICE_DEV_ROLE,
    VET_SERVICE_DEV_ROLE, VISITS_SERVICE_DEV_ROLE,
};
use std::collections::HashMap;
use std::sync::LazyLock;
use uuid::Uuid;

#[derive(Debug)]
pub struct ServiceDescriptor {
    pub docker_service: &'static str,
    pub docker_db: &'static str,
    pub logs_role: Option<Uuid>,
    pub restart_role: Option<Uuid>,
}

pub static SERVICES: LazyLock<HashMap<&'static str, ServiceDescriptor>> = LazyLock::new(|| {
    let mut m = HashMap::new();

    m.insert(
        "petclinic-portal",
        ServiceDescriptor {
            docker_service: "petclinic-frontend",
            docker_db: "",
            logs_role: None,
            restart_role: None,
        },
    );
    m.insert(
        "employee-portal",
        ServiceDescriptor {
            docker_service: "employee-frontend",
            docker_db: "",
            logs_role: None,
            restart_role: None,
        },
    );

    m.insert(
        "api-gateway",
        ServiceDescriptor {
            docker_service: "api-gateway",
            docker_db: "",
            logs_role: None,
            restart_role: None,
        },
    );

    m.insert(
        "visits",
        ServiceDescriptor {
            docker_service: "visits-service-new",
            docker_db: "mongo-visits",
            logs_role: Some(VISITS_SERVICE_DEV_ROLE),
            restart_role: Some(VISITS_SERVICE_DEV_ROLE),
        },
    );
    m.insert(
        "inventory",
        ServiceDescriptor {
            docker_service: "inventory-service",
            docker_db: "mongo-inventory",
            logs_role: Some(INVENTORY_SERVICE_DEV_ROLE),
            restart_role: Some(INVENTORY_SERVICE_DEV_ROLE),
        },
    );
    m.insert(
        "vet",
        ServiceDescriptor {
            docker_service: "vet-service",
            docker_db: "mongo-vet",
            logs_role: Some(VET_SERVICE_DEV_ROLE),
            restart_role: Some(VET_SERVICE_DEV_ROLE),
        },
    );
    m.insert(
        "customers",
        ServiceDescriptor {
            docker_service: "customers-service-reactive",
            docker_db: "mongo-customers",
            logs_role: Some(CUSTOMERS_SERVICE_DEV_ROLE),
            restart_role: Some(CUSTOMERS_SERVICE_DEV_ROLE),
        },
    );
    m.insert(
        "auth",
        ServiceDescriptor {
            docker_service: "auth-service",
            docker_db: "mysql-auth",
            logs_role: Some(AUTH_SERVICE_DEV_ROLE),
            restart_role: Some(AUTH_SERVICE_DEV_ROLE),
        },
    );
    m.insert(
        "billing",
        ServiceDescriptor {
            docker_service: "billing-service",
            docker_db: "mongo-billing",
            logs_role: Some(BILLING_SERVICE_DEV_ROLE),
            restart_role: Some(BILLING_SERVICE_DEV_ROLE),
        },
    );
    m.insert(
        "products",
        ServiceDescriptor {
            docker_service: "products-service",
            docker_db: "mongo-products",
            logs_role: Some(PRODUCTS_SERVICE_DEV_ROLE),
            restart_role: Some(PRODUCTS_SERVICE_DEV_ROLE),
        },
    );
    m.insert(
        "cart",
        ServiceDescriptor {
            docker_service: "cart-service",
            docker_db: "mongo-carts",
            logs_role: Some(CART_SERVICE_DEV_ROLE),
            restart_role: Some(CART_SERVICE_DEV_ROLE),
        },
    );

    // ========= Go services =========
    m.insert(
        "mailer",
        ServiceDescriptor {
            docker_service: "mailer-service",
            docker_db: "",
            logs_role: None,
            restart_role: None,
        },
    );
    m.insert(
        "files",
        ServiceDescriptor {
            docker_service: "files-service",
            docker_db: "mysql-files",
            logs_role: None,
            restart_role: None,
        },
    );

    m
});
