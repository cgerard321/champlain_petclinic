package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.CartServiceClient;
import com.petclinic.bffapigateway.dtos.Cart.CartResponseDTO;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CartController.class, CartServiceClient.class })
@WebFluxTest(controllers = CartController.class)
@AutoConfigureWebTestClient
public class CartControllerUnitTest {

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

    @Test
    void testGetCartByCustomerId_Success() {
        //arrange
        String customerId = "98f7b33a-d62a-420a-a84a-05a27c85fc91";
        CartResponseDTO mockResponse = new CartResponseDTO();
        mockResponse.setCartId(customerId);
        mockResponse.setCustomerId("customer1");

        when(cartServiceClient.getCartByCustomerId(customerId)).thenReturn(Mono.just(mockResponse));

        //act
        client.get()
                .uri("/api/v2/gateway/carts/customer/" + customerId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponseDTO.class)
                .consumeWith(response -> {
                    CartResponseDTO responseBody = response.getResponseBody();
                    assertEquals(customerId, responseBody.getCartId());
                    assertEquals("customer1", responseBody.getCustomerId());
                });

        //assert
        verify(cartServiceClient, times(1)).getCartByCustomerId(customerId);
    }

    @Test
    void testGetCartByCustomerId_NotFound() {
        //arrange
        String customerId = "non-existent-customer-id";
        when(cartServiceClient.getCartByCustomerId(customerId)).thenReturn(Mono.empty());

        //act
        client.get()
                .uri("/api/v2/gateway/carts/customer/" + customerId)
                .exchange()
                .expectStatus().isNotFound();

        //assert
        verify(cartServiceClient, times(1)).getCartByCustomerId(customerId);
    }
    /*
    --------------------------------------------------------------------------------------------------------------------
        from here it's the tests related to the wishlist
    */

    @Test
    void testCheckoutCart_Success() {
        // Arrange
        when(cartServiceClient.checkoutCart("cartId123")).thenReturn(Mono.empty()); // Simulate a successful cart checkout action

        // Act
        client.post()
                .uri("/api/v2/gateway/carts/cartId123/checkout")
                .exchange()
                .expectStatus().isOk();

        // Assert
        verify(cartServiceClient, times(1)).checkoutCart("cartId123");
    }

    @Test
    void testCheckoutCart_Failure() {
        // Arrange
        when(cartServiceClient.checkoutCart("cartId123")).thenReturn(Mono.error(new RuntimeException("Checkout failed"))); // Simulate a failed cart checkout action

        // Act
        client.post()
                .uri("/api/v2/gateway/carts/cartId123/checkout")
                .exchange()
                .expectStatus().isBadRequest();

        // Assert
        verify(cartServiceClient, times(1)).checkoutCart("cartId123");
    }

    @Test
    void testCheckoutCart_Success() {
        // Arrange
        when(cartServiceClient.checkoutCart("cartId123")).thenReturn(Mono.empty()); // Simulate a successful cart checkout action

        // Act
        client.post()
                .uri("/api/v2/gateway/carts/cartId123/checkout")
                .exchange()
                .expectStatus().isOk();

        // Assert
        verify(cartServiceClient, times(1)).checkoutCart("cartId123");
    }

    @Test
    void testCheckoutCart_Failure() {
        // Arrange
        when(cartServiceClient.checkoutCart("cartId123")).thenReturn(Mono.error(new RuntimeException("Checkout failed"))); // Simulate a failed cart checkout action

        // Act
        client.post()
                .uri("/api/v2/gateway/carts/cartId123/checkout")
                .exchange()
                .expectStatus().isBadRequest();

        // Assert
        verify(cartServiceClient, times(1)).checkoutCart("cartId123");
    }


}
