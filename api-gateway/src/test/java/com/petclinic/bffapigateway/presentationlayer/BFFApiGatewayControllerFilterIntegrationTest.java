package com.petclinic.bffapigateway.presentationlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.config.GlobalExceptionHandler;
import com.petclinic.bffapigateway.domainclientlayer.*;
import com.petclinic.bffapigateway.dtos.Auth.Login;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Filters.IsUserFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.JwtTokenFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.JwtTokenUtil;
import com.petclinic.bffapigateway.utils.Security.Filters.RoleFilter;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static reactor.core.publisher.Mono.when;

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

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired private WebTestClient client;
    @MockBean
    private CustomersServiceClient customersServiceClient;
    @MockBean
    private VisitsServiceClient visitsServiceClient;
    @MockBean
    private VetsServiceClient vetsServiceClient;
    @MockBean
    private AuthServiceClient authServiceClient;
    @MockBean
    private BillServiceClient billServiceClient;
    @MockBean
    private InventoryServiceClient inventoryServiceClient;
    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @Test
    void testGetAllCustomers_ShouldReturnUnauthorized() {
        client.get()
                .uri("/api/gateway/owners")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isUnauthorized();
    }



    @Test
    void testGetAllCustomers_ShouldReturnOk() {


        Mockito.when(jwtTokenUtil.getTokenFromRequest(any(ServerWebExchange.class)))
                .thenReturn("valid.token.signed");

        Mono<ResponseEntity<Flux<String>>> validationResponse = Mono.just(ResponseEntity.ok().build());
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



    //@Test
    void getUserByIdWithWrongAccount_ShouldFail(){

        Mockito.when(jwtTokenUtil.getTokenFromRequest(any(ServerWebExchange.class)))
                .thenReturn("valid.token.signed");

        Mono<ResponseEntity<Flux<String>>> validationResponse = Mono.just(ResponseEntity.ok().build());
        Mockito.when(authServiceClient.validateToken(anyString()))
                .thenReturn(validationResponse);

        Mockito.when(jwtTokenUtil.getIdFromToken(anyString()))
                .thenReturn("BADUUID");


        client.get()
                .uri("/api/gateway/owners/UUID")
                .accept(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                .acceptCharset(StandardCharsets.UTF_8)
                .cookie("Bearer", "valid.token.signed")
                .exchange()
                .expectStatus().isUnauthorized();
    }


}