package com.petclinic.authservice.businesslayer;

import com.petclinic.authservice.datalayer.roles.Role;
import com.petclinic.authservice.datalayer.roles.RoleRepo;
import com.petclinic.authservice.datalayer.roles.RoleRequestModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepo roleRepo;

    @Override
    public Role createRole(RoleRequestModel roleRequestModel) {
        Role role = new Role();
        role.setName(roleRequestModel.getName());
        Role savedRole = roleRepo.save(role);
        log.info("Created role with ID: {} and name: {}", savedRole.getId(), savedRole.getName());
        return savedRole;
    }

    @Override
    public List<Role> getAllRoles() {
        log.info("Fetching all roles");
        return roleRepo.findAll();
    }

    @Override
    public Role updateRole(Long roleId, RoleRequestModel roleRequestModel) {
        Role role = roleRepo.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));
        role.setName(roleRequestModel.getName());
        Role updatedRole = roleRepo.save(role);
        log.info("Updated role with ID: {} and name: {}", updatedRole.getId(), updatedRole.getName());
        return updatedRole;
    }

    @Override
    public Role getRoleById(Long roleId) {
        Role role = roleRepo.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));
        log.info("Fetched role with ID: {} and name: {}", role.getId(), role.getName());
        return role;
    }
}