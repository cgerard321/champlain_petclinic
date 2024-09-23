package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CartController.class, CartServiceClient.class })
@WebFluxTest(controllers = CartController.class)
@AutoConfigureWebTestClient
public class CartControllerTest {

    @Autowired
    private WebTestClient client;

    @MockBean
    private CartServiceClient cartServiceClient;

    @Test
    void testClearCart_Success() {
        // Arrange
        when(cartServiceClient.clearCart("cartId123")).thenReturn(Mono.empty()); // Simulate a successful cart clear action

        // Act
        client.delete()
                .uri("/api/v2/gateway/carts/cartId123/clear")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Cart successfully cleared");

        // Assert
        verify(cartServiceClient, times(1)).clearCart("cartId123");
    }


}
