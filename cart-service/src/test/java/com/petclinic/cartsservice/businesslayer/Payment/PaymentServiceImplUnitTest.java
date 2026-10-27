package com.petclinic.cartsservice.businesslayer.Payment;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.presentationlayer.Payment.StripeProductRequestModel;
import com.petclinic.cartsservice.presentationlayer.Payment.StripeResponseModel;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplUnitTest {
    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private static final String CART_ID = "cart-001";

    private StripeProductRequestModel createRequest(String cartId) {
        return StripeProductRequestModel.builder()
                .cartId(cartId)
                .build();
    }

    private CartProduct createCartProduct(
            double price, int quantity) {

        return CartProduct.builder()
                .productName("Dog Food")
                .productSalePrice(price)
                .quantityInCart(quantity)
                .build();
    }

    private Cart createCart(List<CartProduct> products) {
        return Cart.builder()
                .cartId(CART_ID)
                .products(products)
                .build();
    }

    @Test
    void createStripeCheckoutSession_withMismatchedCartIds_returnsInvalidInputException() {

        // Arrange
        StripeProductRequestModel request =
                createRequest("cart-999");

        // Act
        Mono<StripeResponseModel> result =
                paymentService.createStripeCheckoutSession(
                        request, CART_ID);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(error ->
                        error instanceof InvalidInputException
                                && error.getMessage().equals(
                                "Cart ids does not match."))
                .verify();

        verifyNoInteractions(cartRepository);
    }

    @Test
    void createStripeCheckoutSession_withCartNotFound_returnsNotFoundException() {

        // Arrange
        StripeProductRequestModel request =
                createRequest(CART_ID);

        when(cartRepository.findCartByCartId(CART_ID))
                .thenReturn(Mono.empty());

        // Act
        Mono<StripeResponseModel> result =
                paymentService.createStripeCheckoutSession(
                        request, CART_ID);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(error ->
                        error instanceof NotFoundException
                                && error.getMessage().equals(
                                "Cart not found."))
                .verify();

        verify(cartRepository).findCartByCartId(CART_ID);
    }

    @Test
    void createStripeCheckoutSession_withEmptyCart_returnsInvalidInputException() {

        // Arrange
        StripeProductRequestModel request =
                createRequest(CART_ID);

        Cart cart = createCart(List.of());

        when(cartRepository.findCartByCartId(CART_ID))
                .thenReturn(Mono.just(cart));

        // Act
        Mono<StripeResponseModel> result =
                paymentService.createStripeCheckoutSession(
                        request, CART_ID);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(error ->
                        error instanceof InvalidInputException
                                && error.getMessage().equals(
                                "No products found in cart."))
                .verify();

        verify(cartRepository).findCartByCartId(CART_ID);
    }

    @Test
    void createStripeCheckoutSession_withInvalidPrice_returnsInvalidInputException() {

        // Arrange
        StripeProductRequestModel request =
                createRequest(CART_ID);

        CartProduct product = createCartProduct(0.0, 2);

        Cart cart = createCart(List.of(product));

        when(cartRepository.findCartByCartId(CART_ID))
                .thenReturn(Mono.just(cart));

        // Act
        Mono<StripeResponseModel> result =
                paymentService.createStripeCheckoutSession(
                        request, CART_ID);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(error ->
                        error instanceof InvalidInputException
                                && error.getMessage().equals(
                                "Invalid price amount."))
                .verify();

        verify(cartRepository).findCartByCartId(CART_ID);
    }

    @Test
    void createStripeCheckoutSession_withInvalidQuantity_returnsInvalidInputException() {

        // Arrange
        StripeProductRequestModel request =
                createRequest(CART_ID);

        CartProduct product = createCartProduct(19.99, 0);

        Cart cart = createCart(List.of(product));

        when(cartRepository.findCartByCartId(CART_ID))
                .thenReturn(Mono.just(cart));

        // Act
        Mono<StripeResponseModel> result =
                paymentService.createStripeCheckoutSession(
                        request, CART_ID);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(error ->
                        error instanceof InvalidInputException
                                && error.getMessage().equals(
                                "Invalid price amount."))
                .verify();

        verify(cartRepository).findCartByCartId(CART_ID);
    }

}