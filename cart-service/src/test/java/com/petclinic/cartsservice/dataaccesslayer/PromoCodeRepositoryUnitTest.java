//package com.petclinic.cartsservice.dataaccesslayer;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
//import org.springframework.dao.DuplicateKeyException;
//import org.springframework.test.context.ActiveProfiles;
//import reactor.test.StepVerifier;
//
//import java.time.LocalDateTime;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@DataMongoTest
//@ActiveProfiles("test")
//class PromoCodeRepositoryUnitTest {
//
//    @Autowired
//    private PromoRepository promoRepository;
//
//    private final PromoCode promoCode1 = PromoCode.builder()
//            .id("promo123")
//            .code("SUMMER2024")
//            .Name("Summer Promo")
//            .expirationDate(LocalDateTime.parse("2024-12-31T23:59:59"))
//            .isActive(true)
//            .build();
//
//    private final String nonExistentPromoId = "nonExistentPromoId";
//
//    @BeforeEach
//    public void setUp() {
//        // Clear the repository before each test
//        StepVerifier
//                .create(promoRepository.deleteAll())
//                .expectNextCount(0)
//                .verifyComplete();
//    }
//
//    @Test
//    void findPromoCodeById_withExistingId_thenReturnPromoCode() {
//        // Arrange: Save promoCode1 in the repository
//        StepVerifier.create(promoRepository.save(promoCode1))
//                .consumeNextWith(savedPromoCode -> {
//                    assertNotNull(savedPromoCode);
//                    assertEquals(promoCode1.getId(), savedPromoCode.getId());
//                })
//                .verifyComplete();
//
//        // Act: Retrieve the promoCode by its ID
//        StepVerifier.create(promoRepository.findPromoCodeById(promoCode1.getId()))
//                .assertNext(foundPromoCode -> {
//                    assertNotNull(foundPromoCode);
//                    assertEquals(promoCode1.getId(), foundPromoCode.getId());
//                    assertEquals(promoCode1.getCode(), foundPromoCode.getCode());
//                    assertEquals(promoCode1.getName(), foundPromoCode.getName());
//                    assertEquals(promoCode1.getExpirationDate(), foundPromoCode.getExpirationDate());
//                })
//                .verifyComplete();
//    }
//
//    @Test
//    void findPromoCodeById_withNonExistentId_thenReturnEmpty() {
//        // Act: Try to find a promo code by a non-existent ID
//        StepVerifier
//                .create(promoRepository.findPromoCodeById(nonExistentPromoId))
//                .expectNextCount(0)  // Expecting no elements in the Mono
//                .verifyComplete();
//    }
//
//    @Test
//    void savePromoCode_whenValidPromoCode_thenReturnSavedPromoCode() {
//        // Act: Save the promoCode
//        StepVerifier.create(promoRepository.save(promoCode1))
//                .assertNext(savedPromoCode -> {
//                    assertNotNull(savedPromoCode);
//                    assertEquals(promoCode1.getId(), savedPromoCode.getId());
//                    assertEquals(promoCode1.getCode(), savedPromoCode.getCode());
//                    assertEquals(promoCode1.getName(), savedPromoCode.getName());
//                })
//                .verifyComplete();
//    }
//
//    @Test
//    void deletePromoCode_whenPromoCodeExists_thenDeleteSuccessfully() {
//        // Arrange: Save promoCode1 in the repository
//        StepVerifier.create(promoRepository.save(promoCode1))
//                .consumeNextWith(savedPromoCode -> {
//                    assertNotNull(savedPromoCode);
//                    assertEquals(promoCode1.getId(), savedPromoCode.getId());
//                })
//                .verifyComplete();
//
//        // Act: Delete the promoCode
//        StepVerifier.create(promoRepository.delete(promoCode1))
//                .verifyComplete();
//
//        // Assert: Ensure the promoCode no longer exists in the repository
//        StepVerifier.create(promoRepository.findPromoCodeById(promoCode1.getId()))
//                .expectNextCount(0)  // Expecting no elements because it was deleted
//                .verifyComplete();
//    }
//
//    @Test
//    void deletePromoCode_whenPromoCodeDoesNotExist_thenDoNothing() {
//        // Act: Try to delete a non-existent promo code
//        StepVerifier.create(promoRepository.deleteById(nonExistentPromoId))
//                .verifyComplete();
//
//        // Assert: Ensure the promo code with non-existent ID is not present
//        StepVerifier.create(promoRepository.findPromoCodeById(nonExistentPromoId))
//                .expectNextCount(0)
//                .verifyComplete();
//    }
//
////    @Test
////    void findAllByExpirationDateGreaterThanEqualAndActiveIsTrue_withValidAndExpiredPromos_thenReturnOnlyActivePromos() {
////        // Arrange: Set up two promo codes, one active and valid, the other expired
////        PromoCode activePromoCode = PromoCode.builder()
////                .id("activePromo")
////                .code("ACTIVE2024")
////                .Name("Active Promo")
////                .expirationDate(LocalDateTime.parse("2024-12-31T23:59:59"))
////                .isActive(true)
////                .build();
////
////        PromoCode expiredPromoCode = PromoCode.builder()
////                .id("expiredPromo")
////                .code("EXPIRED2023")
////                .Name("Expired Promo")
////                .expirationDate(LocalDateTime.parse("2023-01-01T23:59:59"))
////                .isActive(true)
////                .build();
////
////        // Save both promo codes in the repository
////        StepVerifier.create(promoRepository.save(activePromoCode)).expectNextCount(1).verifyComplete();
////        StepVerifier.create(promoRepository.save(expiredPromoCode)).expectNextCount(1).verifyComplete();
////
////        // Act: Query for active promo codes based on current date and active status
////        LocalDateTime currentDate = LocalDateTime.now();
////        StepVerifier.create(promoRepository.findAllByExpirationDateGreaterThanEqual(currentDate))
////                .assertNext(foundPromoCode -> {
////                    assertNotNull(foundPromoCode);
////                    assertEquals("activePromo", foundPromoCode.getId()); // Only active promo code should be returned
////                    assertEquals("ACTIVE2024", foundPromoCode.getCode());
////                    assertEquals("Active Promo", foundPromoCode.getName());
////                    assertEquals(LocalDateTime.parse("2024-12-31T23:59:59"), foundPromoCode.getExpirationDate());
////                })
////                .verifyComplete();
////
////        // Ensure the expired promo is not returned
////        StepVerifier.create(promoRepository.findPromoCodeById("expiredPromo"))
////                .expectNextMatches(promo -> promo.getExpirationDate().isBefore(currentDate) && promo.isActive())
////                .verifyComplete();
////    }
//
//
//    @Test
//    void findPromoCodeByCode_withExistingCode_thenReturnPromoCode() {
//        // Arrange: Save promoCode1 in the repository
//        StepVerifier.create(promoRepository.save(promoCode1))
//                .consumeNextWith(savedPromoCode -> {
//                    assertNotNull(savedPromoCode);
//                    assertEquals(promoCode1.getCode(), savedPromoCode.getCode());
//                })
//                .verifyComplete();
//
//        // Act: Retrieve the promoCode by its code
//        StepVerifier.create(promoRepository.findPromoCodeByCode(promoCode1.getCode()))
//                .assertNext(foundPromoCode -> {
//                    assertNotNull(foundPromoCode);
//                    assertEquals(promoCode1.getId(), foundPromoCode.getId());
//                    assertEquals(promoCode1.getCode(), foundPromoCode.getCode());
//                    assertEquals(promoCode1.getName(), foundPromoCode.getName());
//                    assertEquals(promoCode1.getExpirationDate(), foundPromoCode.getExpirationDate());
//                    assertTrue(foundPromoCode.isActive());
//                })
//                .verifyComplete();
//    }
//
//
//    @Test
//    void findPromoCodeByCode_withNonExistentCode_thenReturnEmpty() {
//        // Act: Try to find a promo code by a non-existent code
//        StepVerifier.create(promoRepository.findPromoCodeByCode("NONEXISTENTCODE"))
//                .expectNextCount(0)
//                .verifyComplete();
//    }
//
//    @Test
//    void updatePromoCode_whenValid_thenUpdateSuccessfully() {
//        // Arrange: Save promoCode1 in the repository
//        StepVerifier.create(promoRepository.save(promoCode1))
//                .expectNextCount(1)
//                .verifyComplete();
//
//        // Modify promoCode1
//        PromoCode updatedPromo = PromoCode.builder()
//                .id("promo123")
//                .code("SUMMER2024UPDATED")
//                .Name("Summer Promo Updated")
//                .expirationDate(LocalDateTime.now().plusDays(60))
//                .isActive(true)
//                .build();
//
//        // Act: Update the promo code
//        StepVerifier.create(promoRepository.save(updatedPromo))
//                .assertNext(savedPromo -> {
//                    assertNotNull(savedPromo);
//                    assertEquals("SUMMER2024UPDATED", savedPromo.getCode());
//                    assertEquals("Summer Promo Updated", savedPromo.getName());
//                    // Note: Due to potential slight differences in time, use a range for date assertion
//                    assertTrue(savedPromo.getExpirationDate().isAfter(LocalDateTime.now().plusDays(59)));
//                })
//                .verifyComplete();
//
//        // Assert: Retrieve the updated promo code
//        StepVerifier.create(promoRepository.findPromoCodeById("promo123"))
//                .assertNext(foundPromo -> {
//                    assertEquals("SUMMER2024UPDATED", foundPromo.getCode());
//                    assertEquals("Summer Promo Updated", foundPromo.getName());
//                })
//                .verifyComplete();
//    }
//
//
//
//
//
//
//
//
//
//}
