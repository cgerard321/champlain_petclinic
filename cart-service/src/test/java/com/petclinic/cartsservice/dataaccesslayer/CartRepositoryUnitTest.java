package com.petclinic.cartsservice.dataaccesslayer;

import com.petclinic.cartsservice.domainclientlayer.ProductResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class CartRepositoryUnitTest {

    @Autowired
    private CartRepository cartRepository;

    private final String nonExistentCartId = "06a7d573-bcab-4db3-956f-773324b92a80";

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

    @BeforeEach
    public void setUp() {
        StepVerifier
                .create(cartRepository.deleteAll())
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