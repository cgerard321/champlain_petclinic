use rocket::serde::{Deserialize, Serialize};

#[derive(Serialize, Debug)]
pub struct LogResponseContract {
    pub type_name: String,
    pub message: String,
}

#[derive(Deserialize, Debug)]
pub struct ContainerActionRequestContract {
    pub container_type: String
}
