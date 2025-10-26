pub fn safe_uuid_from_str(s: &str) -> Option<uuid::Uuid> {
    uuid::Uuid::parse_str(s).ok()
}
