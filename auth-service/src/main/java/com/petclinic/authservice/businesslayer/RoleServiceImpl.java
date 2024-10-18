package com.petclinic.authservice.businesslayer;

import com.petclinic.authservice.datalayer.roles.Role;
import com.petclinic.authservice.datalayer.roles.RoleRepo;
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
    public Role createRole(String roleName) {
        log.info("Creating role with name: {}", roleName);
        Role role = Role.builder().name(roleName).build();
        Role savedRole = roleRepo.save(role);
        log.info("Role created with ID: {}", savedRole.getId());
        return savedRole;
    }

    @Override
    public List<Role> getAllRoles() {
        log.info("Fetching all roles");
        return roleRepo.findAll();
    }
}