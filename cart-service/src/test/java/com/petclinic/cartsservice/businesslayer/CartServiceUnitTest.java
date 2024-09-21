package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.domainclientlayer.ProductClient;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.anyString;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CartServiceUnitTest {

    @InjectMocks
    private CartServiceImpl cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductClient productClient;

    List<String> productIds = List.of("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223", "d819e4f4-25af-4d33-91e9-2c45f0071606");

    private final Cart cart1 = Cart.builder()
            .cartId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
            .productIds(productIds)
            .customerId("1")
            .build();

    private final ProductResponseModel product1 = ProductResponseModel.builder()
            .productId("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223")
            .productName("Web Services")
            .productDescription("Learn how to create web services")
            .productSalePrice(100.00)
            .build();

    private final ProductResponseModel product2 = ProductResponseModel.builder()
            .productId("d819e4f4-25af-4d33-91e9-2c45f0071606")
            .productName("Shakespeare's Greatest Works")
            .productDescription("A collection of Shakespeare's greatest works")
            .productSalePrice(50.00)
            .build();

    //UUID for non-existent cart
    private final String nonExistentCartId = "a7d573-bcab-4db3-956f-773324b92a80";

    @Test
    public void whenGetCartById_thenReturnCartResponseModel() {
        //arrange
        when(cartRepository.findCartByCartId(cart1.getCartId())).thenReturn(Mono.just(cart1));
        when(productClient.getProductByProductId("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223")).thenReturn(Mono.just(product1));
        when(productClient.getProductByProductId("d819e4f4-25af-4d33-91e9-2c45f0071606")).thenReturn(Mono.just(product2));

        //act
        Mono<CartResponseModel> result = cartService.getCartByCartId(cart1.getCartId());

        //assert
        StepVerifier.create(result)
                .expectNextMatches(cartResponseModel -> cartResponseModel.getCartId().equals(cart1.getCartId()))
                .verifyComplete();
    }

    @Test
    public void whenGetCartByCartId_withNonExistentCartId_thenThrowNotFoundException() {
        //arrange
        when(cartRepository.findCartByCartId(nonExistentCartId)).thenReturn(Mono.empty());

        //act
        Mono<CartResponseModel> result = cartService.getCartByCartId(nonExistentCartId);

        //assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("Cart id was not found: " + nonExistentCartId))
                .verify();
    }

    @Test
    public void clearCart_Success() {
        Cart mockCart = new Cart("1", "cart1", Arrays.asList("prod1", "prod2"), "customer1");

        // Mock the cart being found
        when(cartRepository.findCartByCartId("cart1")).thenReturn(Mono.just(mockCart));

        // Mock the product client returning product models for each product ID
        ProductResponseModel product1 = new ProductResponseModel("prod1", "Product1", "Desc1", 100.0);
        ProductResponseModel product2 = new ProductResponseModel("prod2", "Product2", "Desc2", 200.0);

        when(productClient.getProductByProductId("prod1")).thenReturn(Mono.just(product1));
        when(productClient.getProductByProductId("prod2")).thenReturn(Mono.just(product2));

        // Mock saving the cart after clearing
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(mockCart));

        StepVerifier.create(cartService.clearCart("cart1"))
                .expectNext(product1)  // Expect the first product to be returned
                .expectNext(product2)  // Expect the second product to be returned
                .verifyComplete();

        // Verify that the cart was saved with an empty product list
        verify(cartRepository, times(1)).save(mockCart);
        assertTrue(mockCart.getProductIds().isEmpty()); // Verify the cart is cleared
    }



    @Test
    public void clearCart_ReturnsProducts() {
        Cart cart = Cart.builder()
                .cartId("cartId1")
                .productIds(List.of("prod1", "prod2"))
                .customerId("customerId1")
                .build();

        ProductResponseModel product1 = new ProductResponseModel("prod1", "Product1", "Description1", 100.0);
        ProductResponseModel product2 = new ProductResponseModel("prod2", "Product2", "Description2", 200.0);

        when(cartRepository.findCartByCartId("cartId1")).thenReturn(Mono.just(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(cart));
        when(productClient.getProductByProductId("prod1")).thenReturn(Mono.just(product1));
        when(productClient.getProductByProductId("prod2")).thenReturn(Mono.just(product2));

        StepVerifier.create(cartService.clearCart("cartId1"))
                .expectNext(product1)
                .expectNext(product2)
                .verifyComplete();

        verify(cartRepository, times(1)).save(cart);
        assertTrue(cart.getProductIds().isEmpty());
    }



}