package com.petclinic.authservice.presentationlayer.User;

import com.petclinic.authservice.datalayer.roles.Role;
import com.petclinic.authservice.datalayer.roles.RoleRequestModel;
import com.petclinic.authservice.datalayer.roles.RoleRepo;
import com.petclinic.authservice.datalayer.user.UserRepo;
import com.petclinic.authservice.security.JwtTokenUtil;
import org.aspectj.lang.annotation.Before;
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
public class RoleControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Test
    void whenGetAllRoles_thenReturnRoleList() {
        String token = jwtTokenUtil.generateToken(userRepo.findAll().get(0));
        Role role1 = Role.builder().name("SUPPORT").build();
        Role role2 = Role.builder().name("PRODUCT_MANAGER").build();
        roleRepo.saveAll(List.of(role1, role2));

        webTestClient.get()
                .uri("/roles")
                .accept(MediaType.APPLICATION_JSON)
                .cookie("Bearer", token)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Role.class)
                .value(roles -> {
                    assertEquals(7, roles.size());
                    assertEquals("SUPPORT", roles.get(5).getName());
                    assertEquals("PRODUCT_MANAGER", roles.get(6).getName());
                });
    }

    @Test
    void whenCreateRole_thenReturnRole() {
        String token = jwtTokenUtil.generateToken(userRepo.findAll().get(0));
        RoleRequestModel roleRequestModel = new RoleRequestModel("SUPPORT");

        webTestClient.post()
                .uri("/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(roleRequestModel)
                .cookie("Bearer", token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Role.class)
                .value(role -> {
                    assertEquals("SUPPORT", role.getName());
                });

        List<Role> roles = roleRepo.findAll();
        Optional<Role> savedRole = roles.stream().filter(role -> "SUPPORT".equals(role.getName())).findFirst();
        assertTrue(savedRole.isPresent());
        assertEquals("SUPPORT", savedRole.get().getName());
    }
  
    @Test
    void whenUpdateRole_thenReturnUpdatedRole() {
        String token = jwtTokenUtil.generateToken(userRepo.findAll().get(0));
        Role role = Role.builder().name("SUPPORT").build();
        roleRepo.save(role);
        RoleRequestModel roleRequestModel = new RoleRequestModel("MANAGER");

        webTestClient.patch()
                .uri("/roles/" + role.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(roleRequestModel)
                .cookie("Bearer", token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Role.class)
                .value(updatedRole -> {
                    assertEquals("MANAGER", updatedRole.getName());
                });

        List<Role> roles = roleRepo.findAll();
        Optional<Role> updatedRole = roles.stream().filter(r -> "MANAGER".equals(r.getName())).findFirst();
        assertTrue(updatedRole.isPresent());
        assertEquals("MANAGER", updatedRole.get().getName());
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
                .value(r -> {
                    assertEquals("SUPPORT", r.getName());
                });
    }
}