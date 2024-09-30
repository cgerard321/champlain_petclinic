package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.ProductClient;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.presentationlayer.CartRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CartServiceUnitTest {

    @InjectMocks
    private CartServiceImpl cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductClient productClient;

    private final CartProduct product1 = CartProduct.builder()
            .productId("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223")
            .productName("Product1")
            .productDescription("Description1")
            .productSalePrice(100.0)
            .quantityInCart(1)
            .averageRating(4.5)
            .build();

    private final CartProduct product2 = CartProduct.builder()
            .productId("d819e4f4-25af-4d33-91e9-2c45f0071606")
            .productName("Product2")
            .productDescription("Description2")
            .productSalePrice(200.0)
            .quantityInCart(1)
            .averageRating(4.0)
            .build();

    private final CartProduct product3 = CartProduct.builder()
            .productId("132d3c5e-dcaa-4a4f-a35e-b8acc37c51c1")
            .productName("Product3")
            .productDescription("Description3")
            .productSalePrice(300.0)
            .quantityInCart(1)
            .averageRating(3.5)
            .build();

    private final List<CartProduct> products = new ArrayList<>(Arrays.asList(product1, product2));


    private final Cart cart1 = Cart.builder()
            .cartId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
            .customerId("1")
            .products(products)
            .build();

    // UUID for non-existent cart
    private final String nonExistentCartId = "a7d573-bcab-4db3-956f-773324b92a80";

//    @Test
//    void whenUpdateCartById_thenReturnCartResponseModel() {
//        productIds.add(product3.getProductId());
//
//        Cart updatedCart = Cart.builder()
//                .id(cart1.getId())
//                .cartId(cart1.getCartId())
//                .customerId(cart1.getCustomerId())
//                .productIds(productIds)
//                .build();
//
//        CartRequestModel cartRequestModel = new CartRequestModel();
//        BeanUtils.copyProperties(updatedCart, cartRequestModel);
//
//        when(cartRepository.findCartByCartId(cart1.getCartId())).thenReturn(Mono.just(cart1));
//        when(productClient.getProductByProductId("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223")).thenReturn(Mono.just(product1));
//        when(productClient.getProductByProductId("d819e4f4-25af-4d33-91e9-2c45f0071606")).thenReturn(Mono.just(product2));
//        when(productClient.getProductByProductId("132d3c5e-dcaa-4a4f-a35e-b8acc37c51c1")).thenReturn(Mono.just(product3));
//        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(updatedCart));
//
//        Mono<CartResponseModel> result = cartService.updateCartByCartId(Mono.just(cartRequestModel), cart1.getCartId());
//        StepVerifier.create(result)
//                .assertNext(cartResponseModel -> {
//                    assertNotNull(cartResponseModel);
//                    assertNotNull(cartResponseModel.getCartId());
//                    assertEquals(updatedCart.getCartId(), cartResponseModel.getCartId());
//                    assertEquals(updatedCart.getProductIds().size(), cartResponseModel.getProducts().size());
//                })
//                .verifyComplete();
//
//        verify(cartRepository).findCartByCartId(cart1.getCartId());
//        verify(cartRepository).save(any(Cart.class));
//    }

//    @Test
//    void whenUpdateCartById_withNonExistentCartId_thenThrowNotFoundException() {
//        when(cartRepository.findCartByCartId(nonExistentCartId)).thenReturn(Mono.empty());
//        Cart updatedCart = Cart.builder()
//                .id(cart1.getId())
//                .cartId(cart1.getCartId())
//                .customerId(cart1.getCustomerId())
//                .productIds(productIds)
//                .build();
//
//        CartRequestModel cartRequestModel = new CartRequestModel();
//        BeanUtils.copyProperties(updatedCart, cartRequestModel);
//
//        Mono<CartResponseModel> result = cartService.updateCartByCartId(Mono.just(cartRequestModel), nonExistentCartId);
//        StepVerifier.create(result)
//                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
//                        && throwable.getMessage().equals("Cart id was not found: " + nonExistentCartId))
//                .verify();
//    }

    @Test
    public void whenGetCartById_thenReturnCartResponseModel() {
        when(cartRepository.findCartByCartId(cart1.getCartId())).thenReturn(Mono.just(cart1));

        Mono<CartResponseModel> result = cartService.getCartByCartId(cart1.getCartId());

        StepVerifier.create(result)
                .expectNextMatches(cartResponseModel -> cartResponseModel.getCartId().equals(cart1.getCartId()))
                .verifyComplete();
    }

    @Test
    public void whenGetCartByCartId_withNonExistentCartId_thenThrowNotFoundException() {
        when(cartRepository.findCartByCartId(nonExistentCartId)).thenReturn(Mono.empty());

        Mono<CartResponseModel> result = cartService.getCartByCartId(nonExistentCartId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("Cart id was not found: " + nonExistentCartId))
                .verify();
    }

    @Test
    public void whenGetAllCarts_thenReturnCartResponseModel() {
        when(cartRepository.findAll()).thenReturn(Flux.just(cart1));

        Flux<CartResponseModel> result = cartService.getAllCarts();

        StepVerifier.create(result)
                .expectNextMatches(cartResponseModel -> cartResponseModel.getCartId().equals(cart1.getCartId()))
                .verifyComplete();
    }


//    @Test
//    public void getCartItemCount_Success() {
//        Cart cart = new Cart();
//        cart.setProductIds(Arrays.asList("prod1", "prod2", "prod3"));
//
//        when(cartRepository.findCartByCartId("cart1")).thenReturn(Mono.just(cart));
//
//        StepVerifier.create(cartService.getCartItemCount("cart1"))
//                .expectNext(3) // Expect 3 items in the cart
//                .verifyComplete();
//    }

    @Test
    public void getCartItemCount_CartNotFound() {
        when(cartRepository.findCartByCartId("cart1")).thenReturn(Mono.empty());

        StepVerifier.create(cartService.getCartItemCount("cart1"))
                .expectError(NotFoundException.class) // Expect a NotFoundException
                .verify();
    }


//    @Test
//    public void clearCart_Success() {
//        Cart mockCart = new Cart("1", "cart1", Arrays.asList("prod1", "prod2"), "customer1");
//
//        when(cartRepository.findCartByCartId("cart1")).thenReturn(Mono.just(mockCart));
//
//        ProductResponseModel product1 = new ProductResponseModel("prod1", "Product1", "Desc1", 100.0);
//        ProductResponseModel product2 = new ProductResponseModel("prod2", "Product2", "Desc2", 200.0);
//
//        when(productClient.getProductByProductId("prod1")).thenReturn(Mono.just(product1));
//        when(productClient.getProductByProductId("prod2")).thenReturn(Mono.just(product2));
//
//        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(mockCart));
//
//        StepVerifier.create(cartService.clearCart("cart1"))
//                .expectNext(product1)
//                .expectNext(product2)
//                .verifyComplete();
//
//        verify(cartRepository, times(1)).save(mockCart);
//        assertTrue(mockCart.getProductIds().isEmpty());
//    }


}
