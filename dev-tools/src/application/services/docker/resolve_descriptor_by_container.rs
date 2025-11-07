use crate::application::services::docker::utils::{ServiceDescriptor, SERVICES};

pub fn resolve_descriptor_by_container(container: &str) -> Option<&'static ServiceDescriptor> {
    let cleaned_name = container.trim();
    log::info!("Resolving descriptor for container: {}", cleaned_name);
    SERVICES.get(cleaned_name)
}