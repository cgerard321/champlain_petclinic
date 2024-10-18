package com.petclinic.authservice.businesslayer;

import com.petclinic.authservice.datalayer.roles.Role;
import com.petclinic.authservice.datalayer.roles.RoleRepo;
import com.petclinic.authservice.datalayer.roles.RoleRequestModel;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

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
    void whenCreateRole_thenReturnRoleResponseModel() {
        RoleRequestModel roleRequestModel = new RoleRequestModel("SUPPORT");
        Role savedRole = Role.builder().id(7L).name(roleRequestModel.getName()).build();

        when(roleRepo.save(any(Role.class))).thenReturn(savedRole);

        Role result = roleService.createRole(roleRequestModel);

        assertEquals(savedRole.getId(), result.getId());
        assertEquals(savedRole.getName(), result.getName());
        verify(roleRepo, times(1)).save(any(Role.class));
    }
}