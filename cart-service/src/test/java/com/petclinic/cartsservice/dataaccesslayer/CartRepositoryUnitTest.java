package com.petclinic.cartsservice.dataaccesslayer;

import com.petclinic.cartsservice.dataaccesslayer.cartproduct.CartProduct;
import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import com.petclinic.cartsservice.presentationlayer.CartRequestModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class CartRepositoryUnitTest {

    @Autowired
    private CartRepository cartRepository;

    private final String nonExistentCartId = "06a7d573-bcab-4db3-956f-773324b92a80";


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
    void saveNewCart_WhenValidInput_ThenSuccess(){
        Cart cartToSave = new Cart();
        cartToSave.setCustomerId("123");
        cartToSave.setCartId("abc-123-xyz");
        StepVerifier.create(cartRepository.save(cartToSave))
                .expectNextMatches(cart1 -> cart1.getCustomerId().equals("123")
                        && cart1.getCartId().equals("abc-123-xyz"))
                .verifyComplete();
        ;
    }

}