package com.petclinic.cartsservice.presentationlayer.Payment;

import com.petclinic.cartsservice.businesslayer.Payment.PaymentServiceImpl;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerUnitTest {
    @Mock
    private PaymentServiceImpl paymentService;

    @InjectMocks
    private PaymentController paymentController;

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

        // Act
        Mono<ResponseEntity<StripeResponseModel>> result =
                paymentController.createStripeCheckoutSession(
                        request, CART_ID);

        // Assert
        StepVerifier.create(result)
                .assertNext(entity -> {
                    assertEquals(HttpStatus.OK,
                            entity.getStatusCode());
                    assertEquals(response, entity.getBody());
                })
                .verifyComplete();

        verify(paymentService).createStripeCheckoutSession(request, CART_ID);
    }

    @Test
    void createStripeCheckoutSession_withCartNotFound_returnsNotFound() {

        // Arrange
        StripeProductRequestModel request =
                new StripeProductRequestModel();
        request.setCartId(CART_ID);

        when(paymentService.createStripeCheckoutSession(
                request, CART_ID))
                .thenReturn(Mono.error(new NotFoundException("Cart not found.")));

        // Act
        Mono<ResponseEntity<StripeResponseModel>> result =
                paymentController.createStripeCheckoutSession(
                        request, CART_ID);

        // Assert
        StepVerifier.create(result)
                .assertNext(entity -> {
                    assertEquals(HttpStatus.NOT_FOUND,
                            entity.getStatusCode());
                    assertNull(entity.getBody());
                })
                .verifyComplete();
    }

    @Test
    void createStripeCheckoutSession_withInvalidInput_returnsBadRequest() {

        // Arrange
        StripeProductRequestModel request =
                new StripeProductRequestModel();
        request.setCartId("cart-999");

        when(paymentService.createStripeCheckoutSession(
                request, CART_ID))
                .thenReturn(Mono.error(new InvalidInputException("Cart ids does not match.")));

        // Act
        Mono<ResponseEntity<StripeResponseModel>> result =
                paymentController.createStripeCheckoutSession(
                        request, CART_ID);

        // Assert
        StepVerifier.create(result)
                .assertNext(entity -> {
                    assertEquals(HttpStatus.BAD_REQUEST,
                            entity.getStatusCode());
                    assertNull(entity.getBody());
                })
                .verifyComplete();
    }

    @Test
    void createStripeCheckoutSession_withEmptyCart_returnsBadRequest() {

        // Arrange
        StripeProductRequestModel request = new StripeProductRequestModel();
        request.setCartId(CART_ID);

        when(paymentService.createStripeCheckoutSession(request, CART_ID))
                .thenReturn(Mono.error(
                        new InvalidInputException("Cart is empty")
                ));

        // Act
        Mono<ResponseEntity<StripeResponseModel>> result =
                paymentController.createStripeCheckoutSession(request, CART_ID);

        // Assert
        StepVerifier.create(result)
                .assertNext(entity -> {
                    assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
                    assertNull(entity.getBody());
                })
                .verifyComplete();

        verify(paymentService).createStripeCheckoutSession(request, CART_ID);
    }

    @Test
    void createStripeCheckoutSession_withInvalidTotal_returnsBadRequest() {

        // Arrange
        StripeProductRequestModel request = new StripeProductRequestModel();
        request.setCartId(CART_ID);

        when(paymentService.createStripeCheckoutSession(request, CART_ID))
                .thenReturn(Mono.error(
                        new InvalidInputException("Invalid total price amount.")
                ));

        // Act
        Mono<ResponseEntity<StripeResponseModel>> result =
                paymentController.createStripeCheckoutSession(request, CART_ID);

        // Assert
        StepVerifier.create(result)
                .assertNext(entity -> {
                    assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
                    assertNull(entity.getBody());
                })
                .verifyComplete();

        verify(paymentService).createStripeCheckoutSession(request, CART_ID);
    }

    @Test
    void createStripeCheckoutSession_withUnexpectedError_propagatesError() {

        // Arrange
        StripeProductRequestModel request = new StripeProductRequestModel();
        request.setCartId(CART_ID);

        when(paymentService.createStripeCheckoutSession(request, CART_ID))
                .thenReturn(Mono.error(
                        new RuntimeException("Unexpected payment error")
                ));

        // Act
        Mono<ResponseEntity<StripeResponseModel>> result =
                paymentController.createStripeCheckoutSession(request, CART_ID);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                "Unexpected payment error".equals(error.getMessage()))
                .verify();

        verify(paymentService).createStripeCheckoutSession(request, CART_ID);
    }

    @Test
    void createStripeCheckoutSession_passesCorrectCartIdToService() {

        // Arrange
        String pathCartId = "cart-001";

        StripeProductRequestModel request = new StripeProductRequestModel();
        request.setCartId(pathCartId);

        StripeResponseModel response =
                new StripeResponseModel(
                        "cs_test_456",
                        "https://checkout.stripe.com/test"
                );

        when(paymentService.createStripeCheckoutSession(request, pathCartId))
                .thenReturn(Mono.just(response));

        // Act
        Mono<ResponseEntity<StripeResponseModel>> result =
                paymentController.createStripeCheckoutSession(request, pathCartId);

        // Assert
        StepVerifier.create(result)
                .assertNext(entity -> {
                    assertEquals(HttpStatus.OK, entity.getStatusCode());
                    assertSame(response, entity.getBody());
                })
                .verifyComplete();

        verify(paymentService).createStripeCheckoutSession(request, pathCartId);
    }

}