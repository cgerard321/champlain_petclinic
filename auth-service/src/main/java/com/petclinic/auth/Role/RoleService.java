package com.petclinic.auth.Role;

public interface RoleService {

    Role createRole(Role role);
    Role createRole(Role role, Role parent);
}
