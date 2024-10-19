package com.petclinic.authservice.presentationlayer.Role;

import com.petclinic.authservice.datalayer.roles.Role;
import com.petclinic.authservice.businesslayer.RoleService;
import com.petclinic.authservice.datalayer.roles.RoleRequestModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@Slf4j
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping()
    public ResponseEntity<Role> createRole(@RequestBody RoleRequestModel roleRequestModel) {
        log.info("Received request to create role with name: {}", roleRequestModel.getName());
        Role role = roleService.createRole(roleRequestModel);
        log.info("Role created with ID: {}", role.getId());
        return ResponseEntity.ok(role);
    }

    @GetMapping()
    public ResponseEntity<List<Role>> getAllRoles() {
        log.info("Received request to fetch all roles");
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }
}