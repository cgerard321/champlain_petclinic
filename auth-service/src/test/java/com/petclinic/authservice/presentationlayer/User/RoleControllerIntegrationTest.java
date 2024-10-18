package com.petclinic.authservice.presentationlayer.User;

import com.petclinic.authservice.datalayer.roles.Role;
import com.petclinic.authservice.datalayer.roles.RoleRequestModel;
import com.petclinic.authservice.datalayer.roles.RoleRepo;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc
public class RoleControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RoleRepo roleRepo;

    @Before("setup")
    public void setup() {
        String baseUri = "http://localhost:" + "9200";
        this.webTestClient = WebTestClient.bindToServer().baseUrl(baseUri).build();
    }

    @Test
    void whenGetAllRoles_thenReturnRoleList() {
        Role role1 = Role.builder().name("ADMIN").build();
        Role role2 = Role.builder().name("USER").build();
        roleRepo.saveAll(List.of(role1, role2));

        webTestClient.get()
                .uri("/roles")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Role.class)
                .value(roles -> {
                    assertEquals(2, roles.size());
                    assertEquals("ADMIN", roles.get(0).getName());
                    assertEquals("USER", roles.get(1).getName());
                });
    }

    @Test
    void whenCreateRole_thenReturnRole() {
        RoleRequestModel roleRequestModel = new RoleRequestModel("SUPPORT");

        webTestClient.post()
                .uri("/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(roleRequestModel)
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
}