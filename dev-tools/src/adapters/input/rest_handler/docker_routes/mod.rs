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
mod mailer_docker;
mod file_docker;
mod gateway_docker;
mod customer_portal_docker;
mod employee_portal_docker;

pub fn docker_routes() -> Vec<rocket::Route> {
    rocket::routes![
        // Gateway
        gateway_docker::gateway_service_logs,
        gateway_docker::restart_gateway_service_container,
        // Customer portal
        customer_portal_docker::customer_portal_service_logs,
        customer_portal_docker::restart_customer_portal_service_container,
        // Employee portal
        employee_portal_docker::employee_portal_service_logs,
        employee_portal_docker::restart_employee_portal_service_container,
        // Portainer
        portainer_docker::portainer_service_logs,
        // Auth service
        auth_service_docker::auth_logs,
        auth_service_docker::restart_auth_container,
        // Billing service
        billing_docker::billing_logs,
        billing_docker::restart_billing_container,
        // Cart service
        cart_docker::carts_logs,
        cart_docker::restart_carts_container,
        // Customer docker
        customer_docker::customers_logs,
        customer_docker::restart_customers_container,
        // Files service
        file_docker::files_logs,
        file_docker::restart_files_container,
        // Inventory service
        inventory_docker::inventory_logs,
        inventory_docker::restart_inventory_container,
        // Mailer service
        mailer_docker::mailer_service_logs,
        mailer_docker::restart_mailer_service_container,
        // Product service
        product_docker::products_logs,
        product_docker::restart_products_container,
        // Vet service
        vet_service_docker::vet_logs,
        vet_service_docker::restart_vet_container,
        // Visit service
        visit_service_docker::visits_logs,
        visit_service_docker::restart_visits_container,
    ]
}
