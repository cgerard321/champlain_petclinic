package com.petclinic.authservice.businesslayer;

import com.petclinic.authservice.datalayer.roles.Role;
import com.petclinic.authservice.datalayer.roles.RoleRepo;
import com.petclinic.authservice.datalayer.roles.RoleRequestModel;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoleServiceUnitTest {

    @Mock
    private RoleRepo roleRepo;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void whenCreateRole_thenReturnRole() {
        RoleRequestModel roleRequestModel = new RoleRequestModel("SUPPORT");
        Role savedRole = Role.builder().id(7L).name(roleRequestModel.getName()).build();

        when(roleRepo.save(any(Role.class))).thenReturn(savedRole);

        Role result = roleService.createRole(roleRequestModel);

        assertEquals(savedRole.getId(), result.getId());
        assertEquals(savedRole.getName(), result.getName());
        verify(roleRepo, times(1)).save(any(Role.class));
    }

    @Test
    void whenGetAllRoles_thenReturnAllRoles() {
        Role role1 = Role.builder().id(1L).name("ADMIN").build();
        Role role2 = Role.builder().id(2L).name("USER").build();
        List<Role> roles = Arrays.asList(role1, role2);

        when(roleRepo.findAll()).thenReturn(roles);

        List<Role> result = roleService.getAllRoles();

        assertEquals(2, result.size());
        assertEquals("ADMIN", result.get(0).getName());
        assertEquals("USER", result.get(1).getName());
        verify(roleRepo, times(1)).findAll();
    }
}