use crate::core::config::SUDO_ROLE_UUID;
use std::collections::HashSet;
use uuid::Uuid;

pub struct AuthContext {
    pub user_id: Uuid,
    pub roles: HashSet<Uuid>,
}

// System User
impl AuthContext {
    /// Creates a new system user instance with predefined sudo roles.
    ///
    /// # Returns
    /// A `Self` instance initialized with the following properties:
    /// - `user_id`: A nil UUID (00000000-0000-0000-0000-000000000000), representing a default system user.
    /// - `roles`: A `HashSet` containing the `SUDO_ROLE_UUID` to designate this user as having sudo privileges.
    ///
    /// # Example
    /// ```
    /// let system_user = AuthContext::system();
    /// assert_eq!(system_user.user_id, Uuid::nil());
    /// assert!(system_user.roles.contains(&SUDO_ROLE_UUID));
    /// ```
    ///
    /// This function is useful for scenarios where a default administrative entity (e.g., system-level user) is required.
    pub fn system() -> Self {
        // Sudo roles
        let sudo_roles = HashSet::from([SUDO_ROLE_UUID]);
        Self {
            user_id: Uuid::nil(),
            roles: sudo_roles,
        }
    }
}
