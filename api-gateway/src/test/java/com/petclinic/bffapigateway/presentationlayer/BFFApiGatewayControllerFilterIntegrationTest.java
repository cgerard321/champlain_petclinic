package com.petclinic.bffapigateway.presentationlayer;

import com.petclinic.bffapigateway.config.GlobalExceptionHandler;
import com.petclinic.bffapigateway.domainclientlayer.*;
import com.petclinic.bffapigateway.dtos.Auth.TokenResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Filters.IsUserFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.JwtTokenFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.JwtTokenUtil;
import com.petclinic.bffapigateway.utils.Security.Filters.RoleFilter;
import com.petclinic.bffapigateway.utils.Utility;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        GlobalExceptionHandler.class,
        BFFApiGatewayController.class,
        AuthServiceClient.class,
        CustomersServiceClient.class,
        VisitsServiceClient.class,
        VetsServiceClient.class,
        BillServiceClient.class,
        InventoryServiceClient.class,
        JwtTokenFilter.class,
        IsUserFilter.class,
        RoleFilter.class
})
@WebFluxTest(controllers = BFFApiGatewayController.class)
@AutoConfigureWebTestClient
class BFFApiGatewayControllerFilterIntegrationTest {

    @Autowired private WebTestClient client;
    @MockBean private CustomersServiceClient customersServiceClient;
    @MockBean private VisitsServiceClient visitsServiceClient;
    @MockBean private VetsServiceClient vetsServiceClient;
    @MockBean private AuthServiceClient authServiceClient;
    @MockBean private BillServiceClient billServiceClient;
    @MockBean private InventoryServiceClient inventoryServiceClient;
    @MockBean private JwtTokenUtil jwtTokenUtil;
    @MockBean private Utility utility;

// @Test
    void testGetAllCustomers_ShouldReturnUnauthorized() {

        client.get()
                .uri("/api/gateway/owners")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isUnauthorized();
    }

//    @Test
    void testGetAllCustomers_ShouldReturnOk() {
        Mockito.when(jwtTokenUtil.getTokenFromRequest(any(ServerWebExchange.class)))
                .thenReturn("valid.token.signed");

        TokenResponseDTO tokenResponseDTO = TokenResponseDTO.builder()
                .userId("UUID")
                .email("username")
                .roles(List.of("ADMIN"))
                .token("valid.token.signed")
                .build();

        Mono<ResponseEntity<TokenResponseDTO>> validationResponse = Mono.just(ResponseEntity.ok(tokenResponseDTO));
        Mockito.when(authServiceClient.validateToken(anyString()))
                .thenReturn(validationResponse);


        client.get()
                .uri("/api/gateway/owners")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .cookie("Bearer", "valid.token.signed")
                .exchange()
                .expectStatus().isOk();
    }

//    @Test
    void getUserByIdWithWrongAccount_ShouldFail(){
        Mockito.when(jwtTokenUtil.getTokenFromRequest(any(ServerWebExchange.class)))
                .thenReturn("valid.token.signed");

        TokenResponseDTO tokenResponseDTO = TokenResponseDTO.builder()
                .userId("UUID")
                .email("username")
                .roles(List.of("ADMIN"))
                .token("valid.token.signed")
                .build();

        Mono<ResponseEntity<TokenResponseDTO>> validationResponse = Mono.just(ResponseEntity.ok(tokenResponseDTO));
        Mockito.when(authServiceClient.validateToken(anyString()))
                .thenReturn(validationResponse);

        client.get()
                .uri("/api/gateway/owners/UUID")
                .accept(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .cookie("Bearer", "valid.token.signed")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}