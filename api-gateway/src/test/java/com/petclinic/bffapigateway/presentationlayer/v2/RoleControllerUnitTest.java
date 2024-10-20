package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.config.GlobalExceptionHandler;
import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.Role;
import com.petclinic.bffapigateway.dtos.Auth.RoleRequestModel;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {RoleController.class, GlobalExceptionHandler.class})
@WebFluxTest(controllers = RoleController.class)
@AutoConfigureWebTestClient
public class RoleControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthServiceClient authServiceClient;

    private final String validJwtToken = "valid-jwt-token";
    private final String baseUrl = "/api/v2/gateway/roles";

    @Test
    public void testUpdateRole_Success() {
        Long roleId = 1L;
        RoleRequestModel roleRequestModel = new RoleRequestModel();
        Role role = new Role();

        when(authServiceClient.updateRole(validJwtToken, roleId, roleRequestModel)).thenReturn(Mono.just(role));

        webTestClient.patch()
                .uri(baseUrl + "/" + roleId)
                .cookie("Bearer", validJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(roleRequestModel))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Role.class)
                .value(response -> assertEquals(role, response));

        verify(authServiceClient, times(1)).updateRole(validJwtToken, roleId, roleRequestModel);
    }

    @Test
    public void testUpdateRole_BadRequest() {
        Long roleId = 1L;
        RoleRequestModel roleRequestModel = new RoleRequestModel();

        when(authServiceClient.updateRole(validJwtToken, roleId, roleRequestModel)).thenReturn(Mono.empty());

        webTestClient.patch()
                .uri(baseUrl + "/" + roleId)
                .cookie("Bearer", validJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(roleRequestModel))
                .exchange()
                .expectStatus().isBadRequest();

        verify(authServiceClient, times(1)).updateRole(validJwtToken, roleId, roleRequestModel);
    }

    @Test
    public void testGetRoleById_Success() {
        Long roleId = 1L;
        Role role = new Role();

        when(authServiceClient.getRoleById(validJwtToken, roleId)).thenReturn(Mono.just(role));

        webTestClient.get()
                .uri(baseUrl + "/" + roleId)
                .cookie("Bearer", validJwtToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Role.class)
                .value(response -> assertEquals(role, response));

        verify(authServiceClient, times(1)).getRoleById(validJwtToken, roleId);
    }

    @Test
    public void testGetRoleById_BadRequest() {
        Long roleId = 1L;

        when(authServiceClient.getRoleById(validJwtToken, roleId)).thenReturn(Mono.empty());

        webTestClient.get()
                .uri(baseUrl + "/" + roleId)
                .cookie("Bearer", validJwtToken)
                .exchange()
                .expectStatus().isBadRequest();

        verify(authServiceClient, times(1)).getRoleById(validJwtToken, roleId);
    }
}
