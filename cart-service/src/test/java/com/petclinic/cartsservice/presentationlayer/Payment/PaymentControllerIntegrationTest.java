package com.petclinic.cartsservice.presentationlayer.Payment;

import com.petclinic.cartsservice.businesslayer.Payment.PaymentServiceImpl;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(PaymentController.class)
class PaymentControllerIntegrationTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private PaymentServiceImpl paymentService;

    private static final String CART_ID = "cart-001";

    @Test
    void createStripeCheckoutSession_withValidCart_returnsOk() {

        // Arrange
        StripeProductRequestModel request =
                new StripeProductRequestModel();
        request.setCartId(CART_ID);

        StripeResponseModel response =
                new StripeResponseModel(
                        "cs_test_123",
                        "https://checkout.stripe.com/test"
                );

        when(paymentService.createStripeCheckoutSession(
                request, CART_ID))
                .thenReturn(Mono.just(response));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/carts/{cartId}/checkout/sessions", CART_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sessionId").isEqualTo("cs_test_123")
                .jsonPath("$.sessionUrl")
                .isEqualTo("https://checkout.stripe.com/test");
    }

    @Test
    void createStripeCheckoutSession_withCartNotFound_returnsNotFound() {

        // Arrange
        StripeProductRequestModel request =
                new StripeProductRequestModel();
        request.setCartId(CART_ID);

        when(paymentService.createStripeCheckoutSession(
                request, CART_ID))
                .thenReturn(Mono.error(
                        new NotFoundException("Cart not found.")));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/carts/{cartId}/checkout/sessions", CART_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().isEmpty();
    }

    @Test
    void createStripeCheckoutSession_withMismatchedCartIds_returnsBadRequest() {

        // Arrange
        StripeProductRequestModel request =
                new StripeProductRequestModel();
        request.setCartId("cart-999");

        when(paymentService.createStripeCheckoutSession(
                request, CART_ID))
                .thenReturn(Mono.error(
                        new InvalidInputException(
                                "Cart ids does not match.")));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/carts/{cartId}/checkout/sessions", CART_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().isEmpty();
    }
}