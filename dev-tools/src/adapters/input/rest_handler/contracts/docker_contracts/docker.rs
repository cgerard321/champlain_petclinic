use rocket::serde::Serialize;

#[derive(Serialize, Debug)]
pub struct LogResponseContract {
    pub type_name: String,
    pub message: String,
}
