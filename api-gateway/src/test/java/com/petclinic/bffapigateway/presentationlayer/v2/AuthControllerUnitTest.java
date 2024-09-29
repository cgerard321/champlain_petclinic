package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.UserDetails;
import com.petclinic.bffapigateway.utils.Security.Filters.JwtTokenFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.RoleFilter;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        UserController.class,
        AuthServiceClient.class,
        CustomersServiceClient.class,
})
@WebFluxTest(controllers = UserController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM,
        classes = {JwtTokenFilter.class,RoleFilter.class}),useDefaultFilters = false)
@AutoConfigureWebTestClient
class AuthControllerUnitTest {
    @Autowired private WebTestClient client;
    @MockBean private CustomersServiceClient customersServiceClient;
    @MockBean private AuthServiceClient authServiceClient;
    private MockWebServer server;

    @InjectMocks
    private UserController userController;

    @Mock
    private CustomersServiceClient customersServiceClientMock;

    @Test
    @DisplayName("Given valid JWT, verify user with redirection")
    void verify_user_with_redirection_shouldSucceedForV2(){
        final String validToken = "some.valid.token";

        // Mocking the behavior of authServiceClient.verifyUserUsingV2Endpoint to return a successful response
        UserDetails user = UserDetails.builder()
                .userId("22222")
                .email("e@mail.com")
                .username("user")
                .roles(Collections.emptySet())
                .build();

        ResponseEntity<UserDetails> responseEntity = ResponseEntity.ok(user);

        when(authServiceClient.verifyUserUsingV2Endpoint(validToken))
                .thenReturn(Mono.just(responseEntity));

        client.get()
                .uri("/api/v2/gateway/verification/{token}", validToken)
                .exchange()
                .expectStatus().isFound()
                .expectHeader().valueEquals("Location", "http://localhost:3000/users/login");
    }

}
