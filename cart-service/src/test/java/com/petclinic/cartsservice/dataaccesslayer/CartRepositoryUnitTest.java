package com.petclinic.cartsservice.dataaccesslayer;

import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.presentationlayer.CartRequestModel;
import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class CartRepositoryUnitTest {

    @Autowired
    private CartRepository cartRepository;

    private final String nonExistentCartId = "06a7d573-bcab-4db3-956f-773324b92a80";
    private final String validCustomerId = "123e4567-e89b-12d3-a456-426614174000";
    private final String nonExistentCustomerId = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    private final String invalidCustomerId = "invalid-customer-id"; // Example of an invalid customer ID


    private final CartProduct product1 = CartProduct.builder()

            .productId("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223")
            .imageId("08a5af6b-3501-4157-9a99-1aa82387b9e4")
            .productName("Product1")
            .productDescription("Description1")
            .productSalePrice(100.0)
            .quantityInCart(1)
            .averageRating(4.5)
            .build();

    private final CartProduct product2 = CartProduct.builder()
            .productId("d819e4f4-25af-4d33-91e9-2c45f0071606")
            .imageId("36b06c01-10f3-4645-9c45-900afc5a8b8a")
            .productName("Product2")
            .productDescription("Description2")
            .productSalePrice(200.0)
            .quantityInCart(1)
            .averageRating(4.0)
            .build();

    private final CartProduct product3 = CartProduct.builder()
            .productId("132d3c5e-dcaa-4a4f-a35e-b8acc37c51c1")
            .imageId("be4e60a4-2369-46e8-abee-20c1a8dce3e5")
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

    @BeforeEach
    public void setUp() {
        StepVerifier
                .create(cartRepository.deleteAll())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void updateCartByCartId_withNonExistingId_thenReturnEmpty(){
        StepVerifier
                .create(cartRepository.findCartByCartId(nonExistentCartId))
                .expectNextCount(0)
                .verifyComplete();
    }



//    @Test
//    void updateCartByCartId_withExistingId_thenReturnCart(){
//
//        StepVerifier.create(cartRepository.save(cart1))
//                .expectNextCount(1)
//                .verifyComplete();
//
//        CartRequestModel cartRequestModel = CartRequestModel.builder()
//                .customerId("1")
//                .productIds(new ArrayList<>(cart1.getProductIds()))
//                .build();
//        cartRequestModel.getProductIds().add(product3.getProductId());
//
//        StepVerifier
//                .create(cartRepository.findCartByCartId(cart1.getCartId()))
//                .assertNext(foundCart -> {
//                    assertNotNull(foundCart);
//                    assertEquals(cart1.getCartId(), foundCart.getCartId());
//
//                    foundCart.getProductIds().add(product3.getProductId());
//                    cartRepository.save(foundCart).subscribe();
//                })
//                .verifyComplete();
//
//        StepVerifier
//                .create(cartRepository.findCartByCartId(cart1.getCartId()))
//                .assertNext(updatedCart -> {
//                    assertNotNull(updatedCart);
//                    assertEquals(cart1.getCartId(), updatedCart.getCartId());
//
//                    assertTrue(updatedCart.getProductIds().contains(product3.getProductId()));
//                })
//                .verifyComplete();
//
//
//    }

    @Test
    void deleteCartByCartId_withExistingId_thenReturnSuccess() {
        StepVerifier.create(cartRepository.save(cart1))
                        .consumeNextWith(insertedCart -> {
                            assertNotNull(insertedCart);
                            assertEquals(cart1.getCartId(), insertedCart.getCartId());
                        })
                                .verifyComplete();

        StepVerifier
                .create(cartRepository.delete(cart1))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void deleteCartByCartId_withNonExistentId_thenReturnEmpty() {
        StepVerifier.create(cartRepository.deleteById(nonExistentCartId))
                .verifyComplete();

        StepVerifier.create(cartRepository.findCartByCartId(nonExistentCartId))
                .expectNextCount(0)
                .verifyComplete();
    }





//    @Test
//    void findCartByCartId_withExistingId_thenReturnCart() {
//        StepVerifier
//                .create(cartRepository.save(cart1))
//                .consumeNextWith(insertedCart -> {
//                    assertNotNull(insertedCart);
//                    assertEquals(cart1.getCartId(), insertedCart.getCartId());
//                    assertEquals(cart1.getCustomerId(), insertedCart.getCustomerId());
//                })
//                .verifyComplete();
//
//        //act and assert
//        StepVerifier
//                .create(cartRepository.findCartByCartId(cart1.getCartId()))
//                .consumeNextWith(foundCart -> {
//                    assertNotNull(foundCart);
//                    assertEquals(cart1.getCartId(), foundCart.getCartId());
//                    assertEquals(cart1.getCustomerId(), foundCart.getCustomerId());
//                })
//                .verifyComplete();
//    }

    @Test
    void findCartByCartId_withNonExistingId_thenReturnNull() {
        StepVerifier
                .create(cartRepository.findCartByCartId(nonExistentCartId))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void getCartByCustomerId_withValidCustomerId_thenReturnCart() {
        List<CartProduct> products = new ArrayList<>(Arrays.asList(product1, product2));
        Cart cart = Cart.builder()
                .cartId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
                .customerId(validCustomerId)
                .products(products)
                .build();

        StepVerifier.create(cartRepository.save(cart))
                .consumeNextWith(savedCart -> {
                    assertNotNull(savedCart);
                    assertEquals(cart.getCartId(), savedCart.getCartId());
                    assertEquals(cart.getCustomerId(), savedCart.getCustomerId());
                })
                .verifyComplete();

        StepVerifier.create(cartRepository.findCartByCustomerId(validCustomerId))
                .consumeNextWith(foundCart -> {
                    assertNotNull(foundCart);
                    assertEquals(cart.getCustomerId(), foundCart.getCustomerId());
                    assertEquals(cart.getCartId(), foundCart.getCartId());
                })
                .verifyComplete();
    }

    @Test
    void getCartByCustomerId_withNonExistentCustomerId_thenReturnEmpty() {
        StepVerifier.create(cartRepository.findCartByCustomerId(nonExistentCustomerId))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void removeProductFromCart_ProductRemovedSuccessfully() {
        // Arrange: Save the cart first to the repository
        StepVerifier.create(cartRepository.save(cart1))
                .expectNextMatches(savedCart -> savedCart.getProducts().contains(product1))
                .verifyComplete();

        // Act: Remove product1 from the cart
        StepVerifier.create(cartRepository.findCartByCartId(cart1.getCartId())
                        .flatMap(cart -> {
                            cart.getProducts().removeIf(p -> p.getProductId().equals(product1.getProductId()));
                            return cartRepository.save(cart);
                        })
                )
                .expectNextMatches(updatedCart ->
                        updatedCart.getProducts().size() == 1 &&
                                updatedCart.getProducts().contains(product2) // Ensure product1 is removed and product2 remains
                )
                .verifyComplete();

        // Verify the cart in the repository has only product2 remaining
        StepVerifier.create(cartRepository.findCartByCartId(cart1.getCartId()))
                .expectNextMatches(cart -> cart.getProducts().size() == 1 && cart.getProducts().contains(product2))
                .verifyComplete();
    }

    @Test
    void removeProductFromCart_ProductNotFoundInCart() {
        // Arrange: Save the cart to the repository
        StepVerifier.create(cartRepository.save(cart1))
                .expectNextMatches(savedCart -> savedCart.getProducts().contains(product1))
                .verifyComplete();

        // Act: Attempt to remove a product (product3) that doesn't exist in the cart
        StepVerifier.create(cartRepository.findCartByCartId(cart1.getCartId())
                        .flatMap(cart -> {
                            Optional<CartProduct> productToRemove = cart.getProducts().stream()
                                    .filter(p -> p.getProductId().equals(product3.getProductId()))
                                    .findFirst();
                            if (productToRemove.isPresent()) {
                                cart.getProducts().remove(productToRemove.get());
                                return cartRepository.save(cart);
                            } else {
                                return Mono.error(new NotFoundException("Product id was not found: " + product3.getProductId()));
                            }
                        })
                )
                .expectErrorMatches(throwable ->
                        throwable instanceof NotFoundException &&
                                throwable.getMessage().equals("Product id was not found: " + product3.getProductId())
                )
                .verify();
    }

    @Test
    void removeProductFromCart_CartNotFound() {
        // Act & Assert: Attempt to remove a product from a non-existent cart
        StepVerifier.create(cartRepository.findCartByCartId(nonExistentCartId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Cart id was not found: " + nonExistentCartId)))
                        .flatMap(cart -> {
                            cart.getProducts().removeIf(p -> p.getProductId().equals(product1.getProductId()));
                            return cartRepository.save(cart);
                        })
                )
                .expectErrorMatches(throwable ->
                        throwable instanceof NotFoundException &&
                                throwable.getMessage().equals("Cart id was not found: " + nonExistentCartId)
                )
                .verify();
    }







}