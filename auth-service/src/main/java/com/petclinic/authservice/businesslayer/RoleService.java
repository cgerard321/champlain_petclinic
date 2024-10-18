package com.petclinic.authservice.businesslayer;

import com.petclinic.authservice.datalayer.roles.Role;

import java.util.List;

public interface RoleService {
    List<Role> getAllRoles();
    Role createRole(String roleName);
}
