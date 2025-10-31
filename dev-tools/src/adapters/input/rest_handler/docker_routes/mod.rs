mod auth_service_docker;
mod utils;
mod vet_service_docker;
mod portainer_docker;
mod visit_service_docker;
mod customer_docker;
mod product_docker;
mod cart_docker;
mod inventory_docker;
mod billing_docker;

pub fn docker_routes() -> Vec<rocket::Route> {
    rocket::routes![
        portainer_docker::portainer_service_logs,
        // Auth service
        auth_service_docker::auth_service_logs,
        auth_service_docker::auth_service_db_logs,
        auth_service_docker::restart_auth_service_container,
        auth_service_docker::restart_auth_service_db_container,
        // Vet service
        vet_service_docker::vet_service_logs,
        vet_service_docker::vet_service_db_logs,
        vet_service_docker::restart_vet_service_container,
        vet_service_docker::restart_vet_service_db_container,
        // Visit service
        visit_service_docker::visits_service_logs,
        visit_service_docker::visits_service_db_logs,
        visit_service_docker::restart_visits_service_container,
        visit_service_docker::restart_visits_service_db_container,
        // Customer service
        customer_docker::customers_service_logs,
        customer_docker::customers_service_db_logs,
        customer_docker::restart_customers_service_container,
        customer_docker::restart_customers_service_db_container,
        // Product service
        product_docker::products_service_logs,
        product_docker::products_service_db_logs,
        product_docker::restart_products_service_container,
        product_docker::restart_products_service_db_container,
        // Cart service
        cart_docker::cart_service_logs,
        cart_docker::cart_service_db_logs,
        cart_docker::restart_cart_service_container,
        cart_docker::restart_cart_service_db_container,
        // Inventory service
        inventory_docker::inventory_service_logs,
        inventory_docker::inventory_service_db_logs,
        inventory_docker::restart_inventory_service_container,
        inventory_docker::restart_inventory_service_db_container,
        // Billing service
        billing_docker::billing_service_logs,
        billing_docker::billing_service_db_logs,
        billing_docker::restart_billing_service_container,
        billing_docker::restart_billing_service_db_container,
    ]
}
