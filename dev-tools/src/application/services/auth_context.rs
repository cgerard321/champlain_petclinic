use crate::core::config::SUDO_ROLE_UUID;
use std::collections::HashSet;
use uuid::Uuid;

pub struct AuthContext {
    pub user_id: Uuid,
    pub roles: HashSet<Uuid>,
}

// System User
impl AuthContext {

    /// Creates a new system-level user with pre-defined "sudo" roles.
    ///
    /// This function initializes a `Self` instance, representing a system-level user.
    /// The user will have a `user_id` of `Uuid::nil()` (indicating a nil UUID) and
    /// be assigned the "sudo" roles, which is a predefined set of administrative permissions.
    ///
    /// # Returns
    ///
    /// Returns an instance of `Self` containing:
    /// - `user_id`: A nil UUID representing the system-level user.
    /// - `roles`: A `HashSet` containing only the `SUDO_ROLE_UUID` indicating
    ///    the user's elevated administrative privileges.
    ///
    /// # Example
    /// ```rust
    /// let system_user = YourStruct::system();
    /// assert_eq!(system_user.user_id, Uuid::nil());
    /// assert!(system_user.roles.contains(&SUDO_ROLE_UUID));
    /// ```
    ///
    /// This function is particularly useful for initializing a special system user
    /// with the highest permissions for administrative or maintenance purposes.
    pub fn system() -> Self {
        // Sudo roles
        let sudo_roles = HashSet::from([SUDO_ROLE_UUID]);
        Self {
            user_id: Uuid::nil(),
            roles: sudo_roles,
        }
    }
}
