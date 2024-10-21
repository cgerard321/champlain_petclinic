package com.petclinic.authservice.presentationlayer.User;

import com.petclinic.authservice.businesslayer.RoleService;
import com.petclinic.authservice.datalayer.roles.Role;
import com.petclinic.authservice.datalayer.roles.RoleRepo;
import com.petclinic.authservice.datalayer.roles.RoleRequestModel;
import com.petclinic.authservice.datalayer.user.UserRepo;
import com.petclinic.authservice.security.JwtTokenUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc
public class RoleControllerIntegrationTestV2 {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Test
    void whenUpdateRole_thenReturnUpdatedRole() {
        String token = jwtTokenUtil.generateToken(userRepo.findAll().get(0));
        RoleRequestModel roleRequestModel = new RoleRequestModel("UPDATED_ROLE");
        Role role = Role.builder().name("SUPPORT").build();
        roleRepo.save(role);

        webTestClient.patch()
                .uri("/roles/" + role.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(roleRequestModel)
                .cookie("Bearer", token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Role.class)
                .value(updatedRole -> {
                    assertEquals("UPDATED_ROLE", updatedRole.getName());
                });

        Optional<Role> updatedRole = roleRepo.findById(role.getId());
        assertTrue(updatedRole.isPresent());
        assertEquals("UPDATED_ROLE", updatedRole.get().getName());
    }

    @Test
    void whenGetRoleById_thenReturnRole() {
        String token = jwtTokenUtil.generateToken(userRepo.findAll().get(0));
        Role role = Role.builder().name("SUPPORT").build();
        roleRepo.save(role);

        webTestClient.get()
                .uri("/roles/" + role.getId())
                .accept(MediaType.APPLICATION_JSON)
                .cookie("Bearer", token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Role.class)
                .value(fetchedRole -> {
                    assertEquals("SUPPORT", fetchedRole.getName());
                });
    }
}