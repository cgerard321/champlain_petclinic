package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.businesslayer.CartService;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = CustomerCartController.class)
class CustomerCartControllerUnitTest {

    @MockBean
    private CartService cartService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void whenGetCartByValidCustomerId_thenReturnCartResponseModel() {
        String validCustomerId = "123e4567-e89b-12d3-a456-426614174000";
        CartResponseModel cartResponseModel = CartResponseModel.builder()
                .customerId(validCustomerId)
                .cartId("cart123")
                .build();

        when(cartService.findCartByCustomerId(validCustomerId)).thenReturn(Mono.just(cartResponseModel));

        webTestClient
                .get()
                .uri("/api/v1/customers/{customerId}/cart", validCustomerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CartResponseModel.class)
                .value(result -> {
                    assertEquals(validCustomerId, result.getCustomerId());
                    assertEquals("cart123", result.getCartId());
                });
    }

    @Test
    void whenGetCartByNonExistingCustomerId_thenReturnNotFound() {
        String nonExistingCustomerId = "123e4567-e89b-12d3-a456-426614174999";
        when(cartService.findCartByCustomerId(nonExistingCustomerId))
                .thenReturn(Mono.error(new NotFoundException("Cart not found for customer id: " + nonExistingCustomerId)));

        webTestClient
                .get()
                .uri("/api/v1/customers/{customerId}/cart", nonExistingCustomerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Cart not found for customer id: " + nonExistingCustomerId);
    }

    @Test
    void whenGetCartWithInvalidCustomerId_thenReturnBadRequest() {
        String invalidCustomerId = "invalid-id";

        webTestClient
                .get()
                .uri("/api/v1/customers/{customerId}/cart", invalidCustomerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided customer id is invalid: " + invalidCustomerId);
    }

    @Test
    void whenGetRecentPurchases_thenReturnList() {
        String customerId = "123e4567-e89b-12d3-a456-426614174001";
        List<CartProduct> items = List.of(CartProduct.builder().productId("prod1").build());

        when(cartService.getRecentPurchasesByCustomerId(customerId)).thenReturn(Mono.just(items));

        webTestClient
                .get()
                .uri("/api/v1/customers/{customerId}/cart/recent-purchases", customerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CartProduct.class)
                .value(result -> assertEquals(1, result.size()));
    }

    @Test
    void whenGetRecommendationPurchases_thenReturnList() {
        String customerId = "123e4567-e89b-12d3-a456-426614174002";
        List<CartProduct> items = List.of(CartProduct.builder().productId("prod2").build());

        when(cartService.getRecommendationPurchasesByCustomerId(customerId)).thenReturn(Mono.just(items));

        webTestClient
                .get()
                .uri("/api/v1/customers/{customerId}/cart/recommendation-purchases", customerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CartProduct.class)
                .value(result -> assertEquals(1, result.size()));
    }
}
