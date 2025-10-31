use uuid::Uuid;
pub const SESSION_EXPIRATION_HR: i64 = 24;
pub const DEFAULT_FILE_TYPE: &str = "bin";
pub const MAX_FILE_SIZE_MB: u64 = 50;
pub const SUDO_ROLE_UUID: Uuid = uuid::uuid!("00000000-0000-0000-0000-000000000000");
pub const ADMIN_ROLE_UUID: Uuid = uuid::uuid!("a48d7b18-ceb7-435b-b8ff-b28531f1a09f");
pub const READER_ROLE_UUID: Uuid = uuid::uuid!("51f20832-79a3-4c05-b4da-ca175cba2ffc");
pub const EDITOR_ROLE_UUID: Uuid = uuid::uuid!("96ee5d72-c27b-4256-8db8-cf49d64e65de");
pub const AUTH_SERVICE_DEV_ROLE: Uuid = uuid::uuid!("00000000-0000-0000-0000-000000000001");
