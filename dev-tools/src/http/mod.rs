mod buckets;
pub mod error;
mod files;
pub mod prelude;

pub fn routes() -> Vec<rocket::Route> {
    routes![files::add_file, files::read_files, buckets::read_buckets]
}
