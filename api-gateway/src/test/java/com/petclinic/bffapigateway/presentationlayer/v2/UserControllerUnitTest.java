package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.presentationlayer.v2.UserController;
import com.petclinic.bffapigateway.config.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Mono;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {UserController.class, GlobalExceptionHandler.class})
@WebFluxTest(controllers = UserController.class)
@AutoConfigureWebTestClient
public class UserControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthServiceClient authServiceClient;

    private final String validJwtToken = "valid-jwt-token";
    private final String baseUrl = "/api/v2/gateway/users";

    @Test
    void disableUser_ShouldReturnOk() {
        String userId = "existing-user-id";

        when(authServiceClient.disableUser(userId, validJwtToken)).thenReturn(Mono.empty());

        webTestClient.patch()
                .uri(baseUrl + "/{userId}/disable", userId)
                .cookie("Bearer", validJwtToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        verify(authServiceClient, times(1)).disableUser(userId, validJwtToken);
    }
    @Test
    void disableUser_ShouldReturnError() {
        String userId = "existing-user-id";
        String errorMessage = "Error disabling user";

        when(authServiceClient.disableUser(userId, validJwtToken))
                .thenReturn(Mono.error(new RuntimeException(errorMessage)));

        webTestClient.patch()
                .uri(baseUrl + "/{userId}/disable", userId)
                .cookie("Bearer", validJwtToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody()
                .consumeWith(response -> {
                    System.out.println("Response received: " + response);
                });

        verify(authServiceClient, times(1)).disableUser(userId, validJwtToken);
    }



    @Test
    void enableUser_ShouldReturnOk() {
        String userId = "existing-user-id";

        when(authServiceClient.enableUser(userId, validJwtToken)).thenReturn(Mono.empty());

        webTestClient.patch()
                .uri(baseUrl + "/{userId}/enable", userId)
                .cookie("Bearer", validJwtToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        verify(authServiceClient, times(1)).enableUser(userId, validJwtToken);
    }


    @Test
    void enableUser_ShouldReturnError() {
        String userId = "existing-user-id";
        String errorMessage = "Error enabling user";

        when(authServiceClient.enableUser(userId, validJwtToken))
                .thenReturn(Mono.error(new RuntimeException(errorMessage)));

        webTestClient.patch()
                .uri(baseUrl + "/{userId}/enable", userId)
                .cookie("Bearer", validJwtToken)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody()
                .consumeWith(response -> {
                    System.out.println("Response received: " + response);
                });

        verify(authServiceClient, times(1)).enableUser(userId, validJwtToken);
    }
}
