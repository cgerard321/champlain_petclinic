package com.petclinic.bffapigateway.utils.Security.Variables;

/**
 * Enum for roles in the application
 *
 * @author Dylan Brassard
 * @since 2023-09-17
 */
public enum Roles {
    ADMIN ( "ADMIN"),
    VET ( "VET"),
    OWNER ( "OWNER"),
    INVENTORY_MANAGER ( "INVENTORY_MANAGER"),
    ANONYMOUS ( "ANONYMOUS"),
    ALL ( "ALL");


    private final String role;

    Roles(String roleName) {
        this.role = roleName;
    }

    public boolean equalsName(String otherName) {
        // (otherName == null) check is not needed because name.equals(null) returns false
        return role.equals(otherName);
    }

    public String toString() {
        return this.role;
    }
}
