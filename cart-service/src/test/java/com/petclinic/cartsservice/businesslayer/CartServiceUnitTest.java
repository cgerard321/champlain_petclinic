package com.petclinic.cartsservice.businesslayer;

import com.petclinic.cartsservice.dataaccesslayer.Cart;
import com.petclinic.cartsservice.dataaccesslayer.CartRepository;
import com.petclinic.cartsservice.domainclientlayer.ProductClient;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.presentationlayer.CartRequestModel;
import com.petclinic.cartsservice.presentationlayer.CartResponseModel;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceUnitTest {

    @InjectMocks
    private CartServiceImpl cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductClient productClient;

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

    private final ProductResponseModel product3 = ProductResponseModel.builder()
            .productId("132d3c5e-dcaa-4a4f-a35e-b8acc37c51c1")
            .productName("Collar")
            .productDescription("The perfect collar for your pet")
            .productSalePrice(20.00)
            .build();

    List<String> productIds = new ArrayList<>(List.of(product1.getProductId(), product2.getProductId()));

    private final Cart cart1 = Cart.builder()
            .cartId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
            .productIds(productIds)
            .customerId("1")
            .build();

    // UUID for non-existent cart
    private final String nonExistentCartId = "a7d573-bcab-4db3-956f-773324b92a80";

    @Test
    void whenUpdateCartById_thenReturnCartResponseModel() {
        productIds.add(product3.getProductId());

        Cart updatedCart = Cart.builder()
                .id(cart1.getId())
                .cartId(cart1.getCartId())
                .customerId(cart1.getCustomerId())
                .productIds(productIds)
                .build();

        CartRequestModel cartRequestModel = new CartRequestModel();
        BeanUtils.copyProperties(updatedCart, cartRequestModel);

        when(cartRepository.findCartByCartId(cart1.getCartId())).thenReturn(Mono.just(cart1));
        when(productClient.getProductByProductId("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223")).thenReturn(Mono.just(product1));
        when(productClient.getProductByProductId("d819e4f4-25af-4d33-91e9-2c45f0071606")).thenReturn(Mono.just(product2));
        when(productClient.getProductByProductId("132d3c5e-dcaa-4a4f-a35e-b8acc37c51c1")).thenReturn(Mono.just(product3));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(updatedCart));

        Mono<CartResponseModel> result = cartService.updateCartByCartId(Mono.just(cartRequestModel), cart1.getCartId());
        StepVerifier.create(result)
                .assertNext(cartResponseModel -> {
                    assertNotNull(cartResponseModel);
                    assertNotNull(cartResponseModel.getCartId());
                    assertEquals(updatedCart.getCartId(), cartResponseModel.getCartId());
                    assertEquals(updatedCart.getProductIds().size(), cartResponseModel.getProducts().size());
                })
                .verifyComplete();

        verify(cartRepository).findCartByCartId(cart1.getCartId());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void whenUpdateCartById_withNonExistentCartId_thenThrowNotFoundException() {
        when(cartRepository.findCartByCartId(nonExistentCartId)).thenReturn(Mono.empty());
        Cart updatedCart = Cart.builder()
                .id(cart1.getId())
                .cartId(cart1.getCartId())
                .customerId(cart1.getCustomerId())
                .productIds(productIds)
                .build();

        CartRequestModel cartRequestModel = new CartRequestModel();
        BeanUtils.copyProperties(updatedCart, cartRequestModel);

        Mono<CartResponseModel> result = cartService.updateCartByCartId(Mono.just(cartRequestModel), nonExistentCartId);
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("Cart id was not found: " + nonExistentCartId))
                .verify();
    }

    @Test
    public void whenGetCartById_thenReturnCartResponseModel() {
        when(cartRepository.findCartByCartId(cart1.getCartId())).thenReturn(Mono.just(cart1));
        when(productClient.getProductByProductId("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223")).thenReturn(Mono.just(product1));
        when(productClient.getProductByProductId("d819e4f4-25af-4d33-91e9-2c45f0071606")).thenReturn(Mono.just(product2));

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
    public void getCartItemCount_Success() {
        Cart cart = new Cart();
        cart.setProductIds(Arrays.asList("prod1", "prod2", "prod3"));

        when(cartRepository.findCartByCartId("cart1")).thenReturn(Mono.just(cart));

        StepVerifier.create(cartService.getCartItemCount("cart1"))
                .expectNext(3) // Expect 3 items in the cart
                .verifyComplete();
    }

    @Test
    public void getCartItemCount_CartNotFound() {
        when(cartRepository.findCartByCartId("cart1")).thenReturn(Mono.empty());

        StepVerifier.create(cartService.getCartItemCount("cart1"))
                .expectError(NotFoundException.class) // Expect a NotFoundException
                .verify();
    }

    @Test
    public void whenCreateCart_thenReturnCartResponse() {

        // arrange
        CartRequestModel cartRequest = new CartRequestModel("123", null);
        Cart expectedCart = new Cart();
        expectedCart.setCartId("abc-123-xyz");
        expectedCart.setCustomerId("123");

        // When
        when(cartRepository.save(any(Cart.class)))
                .thenReturn(Mono.just(expectedCart));

        Mono<CartResponseModel> actualResponse = cartService.createNewCart(cartRequest);

        // Assert
        StepVerifier.create(actualResponse)
                .expectNextMatches(cart -> cart.getCustomerId().equals("123")
                        && cart.getCartId().equals("abc-123-xyz"))
                .verifyComplete();


    }


    @Test
    public void clearCart_Success() {
        Cart mockCart = new Cart("1", "cart1", Arrays.asList("prod1", "prod2"), "customer1");

        when(cartRepository.findCartByCartId("cart1")).thenReturn(Mono.just(mockCart));

        ProductResponseModel product1 = new ProductResponseModel("prod1", "Product1", "Desc1", 100.0);
        ProductResponseModel product2 = new ProductResponseModel("prod2", "Product2", "Desc2", 200.0);

        when(productClient.getProductByProductId("prod1")).thenReturn(Mono.just(product1));
        when(productClient.getProductByProductId("prod2")).thenReturn(Mono.just(product2));

        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(mockCart));

        StepVerifier.create(cartService.clearCart("cart1"))
                .expectNext(product1)
                .expectNext(product2)
                .verifyComplete();

        verify(cartRepository, times(1)).save(mockCart);
        assertTrue(mockCart.getProductIds().isEmpty());
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

    @Test

    public void whenDeleteCartById_withExistingCart_thenCartIsDeleted() {
        // Arrange
        String cartId = cart1.getCartId();
        when(cartRepository.findCartByCartId(cartId)).thenReturn(Mono.just(cart1));
        when(cartRepository.delete(cart1)).thenReturn(Mono.empty());
        when(productClient.getProductByProductId("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223")).thenReturn(Mono.just(product1));
        when(productClient.getProductByProductId("d819e4f4-25af-4d33-91e9-2c45f0071606")).thenReturn(Mono.just(product2));

        // Act
        Mono<CartResponseModel> result = cartService.deleteCartByCartId(cartId);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(cartResponseModel ->
                    cartResponseModel.getCartId().equals(cart1.getCartId()))

                .verifyComplete();

        verify(cartRepository, times(1)).findCartByCartId(cartId);
        verify(cartRepository, times(1)).delete(cart1);
    }

    @Test
    public void whenDeleteCartById_withNonExistentCartId_thenThrowNotFoundException() {
        // Arrange
        when(cartRepository.findCartByCartId(nonExistentCartId)).thenReturn(Mono.empty());

        // Act
        Mono<CartResponseModel> result = cartService.deleteCartByCartId(nonExistentCartId);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("Cart id was not found: " + nonExistentCartId))
                .verify();

        verify(cartRepository, times(1)).findCartByCartId(nonExistentCartId);
        verify(cartRepository, never()).delete(any(Cart.class));
    }

    void getAllCarts_ReturnsCartResponseModelWithProducts() {
        //mocking cart retrieval
        when(cartRepository.findAll()).thenReturn(Flux.just(cart1));

        //mocking product retrieval
        when(productClient.getProductByProductId(product1.getProductId())).thenReturn(Mono.just(product1));
        when(productClient.getProductByProductId(product2.getProductId())).thenReturn(Mono.just(product2));

        //run the test
        StepVerifier.create(cartService.getAllCarts())
                .expectNextMatches(cartResponseModel ->
                        cartResponseModel.getCustomerId().equals("1") &&
                                cartResponseModel.getProducts().size() == 2 && //2 products in the cart
                                cartResponseModel.getProducts().get(0).getProductName().equals("Web Services") &&
                                cartResponseModel.getProducts().get(1).getProductName().equals("Shakespeare's Greatest Works")
                )
                .verifyComplete();

        // Verify interactions
        verify(cartRepository, times(1)).findAll();
        verify(productClient, times(1)).getProductByProductId(product1.getProductId());
        verify(productClient, times(1)).getProductByProductId(product2.getProductId());
    }

    @Test
    void getAllCarts_ReturnsCartWithoutProducts() {
        //mocking cart retrieval with an empty product list
        Cart cartWithNoProducts = Cart.builder()
                .cartId("123")
                .customerId("2")
                .productIds(new ArrayList<>()) //no product IDs
                .build();

        when(cartRepository.findAll()).thenReturn(Flux.just(cartWithNoProducts));

        // Run the test
        StepVerifier.create(cartService.getAllCarts())
                .expectNextMatches(cartResponseModel ->
                        cartResponseModel.getCustomerId().equals("2") &&
                                cartResponseModel.getProducts().isEmpty() //no products in the cart
                )
                .verifyComplete();

        // Verify interactions
        verify(cartRepository, times(1)).findAll();
        verifyNoInteractions(productClient); //no products, so productClient shouldn't be called
    }

    @Test
    void getAllCarts_ReturnsMultipleCartsWithProducts() {
        Cart cart2 = Cart.builder()
                .cartId("456")
                .customerId("3")
                .productIds(List.of(product3.getProductId())) //another cart with 1 product
                .build();

        when(cartRepository.findAll()).thenReturn(Flux.just(cart1, cart2));
        when(productClient.getProductByProductId(product1.getProductId())).thenReturn(Mono.just(product1));
        when(productClient.getProductByProductId(product2.getProductId())).thenReturn(Mono.just(product2));
        when(productClient.getProductByProductId(product3.getProductId())).thenReturn(Mono.just(product3));

        //run the test
        StepVerifier.create(cartService.getAllCarts())
                .expectNextMatches(cartResponseModel -> cartResponseModel.getCustomerId().equals("1"))
                .expectNextMatches(cartResponseModel -> cartResponseModel.getCustomerId().equals("3"))
                .verifyComplete();

        //verify interactions
        verify(cartRepository, times(1)).findAll();
        verify(productClient, times(1)).getProductByProductId(product1.getProductId());
        verify(productClient, times(1)).getProductByProductId(product2.getProductId());
        verify(productClient, times(1)).getProductByProductId(product3.getProductId());
    }

    @Test
    void getAllCarts_ReturnsEmptyListWhenNoCarts() {
        //mocking empty cart retrieval
        when(cartRepository.findAll()).thenReturn(Flux.empty());

        //run the test
        StepVerifier.create(cartService.getAllCarts())
                .expectNextCount(0) //no carts should be returned
                .verifyComplete();

        //verify interactions
        verify(cartRepository, times(1)).findAll();
        verifyNoInteractions(productClient); //no carts, so productClient shouldn't be called
    }

}
