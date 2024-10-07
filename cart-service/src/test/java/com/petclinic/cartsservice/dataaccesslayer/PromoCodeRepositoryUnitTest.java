package com.petclinic.cartsservice.dataaccesslayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@ActiveProfiles("test")
class PromoCodeRepositoryUnitTest {

    @Autowired
    private PromoRepository promoRepository;

    private final PromoCode promoCode1 = PromoCode.builder()
            .id("promo123")
            .code("SUMMER2024")
            .Name("Summer Promo")
            .expirationDate(LocalDateTime.parse("2024-12-31T23:59:59"))
            .isActive(true)
            .build();

    private final String nonExistentPromoId = "nonExistentPromoId";

    @BeforeEach
    public void setUp() {
        // Clear the repository before each test
        StepVerifier
                .create(promoRepository.deleteAll())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void findPromoCodeById_withExistingId_thenReturnPromoCode() {
        // Arrange: Save promoCode1 in the repository
        StepVerifier.create(promoRepository.save(promoCode1))
                .consumeNextWith(savedPromoCode -> {
                    assertNotNull(savedPromoCode);
                    assertEquals(promoCode1.getId(), savedPromoCode.getId());
                })
                .verifyComplete();

        // Act: Retrieve the promoCode by its ID
        StepVerifier.create(promoRepository.findPromoCodeById(promoCode1.getId()))
                .assertNext(foundPromoCode -> {
                    assertNotNull(foundPromoCode);
                    assertEquals(promoCode1.getId(), foundPromoCode.getId());
                    assertEquals(promoCode1.getCode(), foundPromoCode.getCode());
                    assertEquals(promoCode1.getName(), foundPromoCode.getName());
                    assertEquals(promoCode1.getExpirationDate(), foundPromoCode.getExpirationDate());
                })
                .verifyComplete();
    }

    @Test
    void findPromoCodeById_withNonExistentId_thenReturnEmpty() {
        // Act: Try to find a promo code by a non-existent ID
        StepVerifier
                .create(promoRepository.findPromoCodeById(nonExistentPromoId))
                .expectNextCount(0)  // Expecting no elements in the Mono
                .verifyComplete();
    }

    @Test
    void savePromoCode_whenValidPromoCode_thenReturnSavedPromoCode() {
        // Act: Save the promoCode
        StepVerifier.create(promoRepository.save(promoCode1))
                .assertNext(savedPromoCode -> {
                    assertNotNull(savedPromoCode);
                    assertEquals(promoCode1.getId(), savedPromoCode.getId());
                    assertEquals(promoCode1.getCode(), savedPromoCode.getCode());
                    assertEquals(promoCode1.getName(), savedPromoCode.getName());
                })
                .verifyComplete();
    }

    @Test
    void deletePromoCode_whenPromoCodeExists_thenDeleteSuccessfully() {
        // Arrange: Save promoCode1 in the repository
        StepVerifier.create(promoRepository.save(promoCode1))
                .consumeNextWith(savedPromoCode -> {
                    assertNotNull(savedPromoCode);
                    assertEquals(promoCode1.getId(), savedPromoCode.getId());
                })
                .verifyComplete();

        // Act: Delete the promoCode
        StepVerifier.create(promoRepository.delete(promoCode1))
                .verifyComplete();

        // Assert: Ensure the promoCode no longer exists in the repository
        StepVerifier.create(promoRepository.findPromoCodeById(promoCode1.getId()))
                .expectNextCount(0)  // Expecting no elements because it was deleted
                .verifyComplete();
    }

    @Test
    void deletePromoCode_whenPromoCodeDoesNotExist_thenDoNothing() {
        // Act: Try to delete a non-existent promo code
        StepVerifier.create(promoRepository.deleteById(nonExistentPromoId))
                .verifyComplete();

        // Assert: Ensure the promo code with non-existent ID is not present
        StepVerifier.create(promoRepository.findPromoCodeById(nonExistentPromoId))
                .expectNextCount(0)
                .verifyComplete();
    }

}
