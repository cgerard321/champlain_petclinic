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

import static org.mockito.Mockito.when;

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


}