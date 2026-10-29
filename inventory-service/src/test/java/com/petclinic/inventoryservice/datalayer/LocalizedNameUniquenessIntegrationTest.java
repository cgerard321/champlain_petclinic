package com.petclinic.inventoryservice.datalayer;

import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import com.petclinic.inventoryservice.utils.DataBaseLoaderService;
import com.petclinic.inventoryservice.utils.LocalizedNameIndexes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Map;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"spring.data.mongodb.port=0",
        "spring.data.mongodb.database=localized_name_uniqueness_test"})
class LocalizedNameUniquenessIntegrationTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final Map<String, String> FIRST_NAME = Map.of("en", "Gauze", "fr", "Gaze");

    @Autowired
    private InventoryRepository inventories;
    @Autowired
    private ProductRepository products;
    @Autowired
    private DataBaseLoaderService loader;
    @Autowired
    private ReactiveMongoTemplate mongoTemplate;
    @Autowired
    private LocalizedNameIndexes localizedNameIndexes;

    @BeforeEach
    void clearData() {
        StepVerifier.create(products.deleteAll().then(inventories.deleteAll()))
                .expectComplete().verify(TIMEOUT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"en", "fr"})
    void competingInventoryWritesCannotStoreTheSameName(String language) {
        Inventory first = Inventory.builder().inventoryId("first").name(FIRST_NAME).build();
        Inventory second = Inventory.builder().inventoryId("second").name(conflictingName(language)).build();

        assertOneWriteSucceeds(inventories.insert(first), inventories.insert(second));

        StepVerifier.create(inventories.count()).expectNext(1L).expectComplete().verify(TIMEOUT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"en", "fr"})
    void competingSupplyWritesCannotStoreTheSameNameWithinInventory(String language) {
        Product first = Product.builder().productId("first").inventoryId("inv1").name(FIRST_NAME).build();
        Product second = Product.builder().productId("second").inventoryId("inv1")
                .name(conflictingName(language)).build();

        assertOneWriteSucceeds(products.insert(first), products.insert(second));

        StepVerifier.create(products.count()).expectNext(1L).expectComplete().verify(TIMEOUT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"en", "fr"})
    void editingInventoryCannotIntroduceDuplicateName(String language) {
        Inventory first = Inventory.builder().inventoryId("first").name(FIRST_NAME).build();
        Inventory second = Inventory.builder().inventoryId("second")
                .name(Map.of("en", "Unique inventory", "fr", "Inventaire distinct")).build();

        Mono<Inventory> change = inventories.insert(first).then(inventories.insert(second))
                .flatMap(saved -> {
                    saved.setName(conflictingName(language));
                    return inventories.save(saved);
                });

        StepVerifier.create(change).expectError(DuplicateKeyException.class).verify(TIMEOUT);
        StepVerifier.create(inventories.findInventoryByInventoryId("second"))
                .assertNext(saved -> assertThat(saved.getName().get("en")).isEqualTo("Unique inventory"))
                .expectComplete().verify(TIMEOUT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"en", "fr"})
    void editingSupplyCannotIntroduceDuplicateNameWithinInventory(String language) {
        Product first = Product.builder().productId("first").inventoryId("inv1").name(FIRST_NAME).build();
        Product second = Product.builder().productId("second").inventoryId("inv1")
                .name(Map.of("en", "Unique supply", "fr", "Article distinct")).build();

        Mono<Product> change = products.insert(first).then(products.insert(second))
                .flatMap(saved -> {
                    saved.setName(conflictingName(language));
                    return products.save(saved);
                });

        StepVerifier.create(change).expectError(DuplicateKeyException.class).verify(TIMEOUT);
        StepVerifier.create(products.findProductByProductId("second"))
                .assertNext(saved -> assertThat(saved.getName().get("en")).isEqualTo("Unique supply"))
                .expectComplete().verify(TIMEOUT);
    }

    @Test
    void sameSupplyNamesAreAllowedInDifferentInventories() {
        Product first = Product.builder().productId("first").inventoryId("inv1").name(FIRST_NAME).build();
        Product second = Product.builder().productId("second").inventoryId("inv2").name(FIRST_NAME).build();

        StepVerifier.create(products.insert(first).then(products.insert(second)).then(products.count()))
                .expectNext(2L).expectComplete().verify(TIMEOUT);
    }

    @Test
    void inventoryNamesCanMatchAcrossDifferentLanguages() {
        Inventory first = Inventory.builder().inventoryId("first").name(FIRST_NAME).build();
        Inventory second = Inventory.builder().inventoryId("second")
                .name(Map.of("en", "Gaze", "fr", "Gauze")).build();

        StepVerifier.create(inventories.insert(first).then(inventories.insert(second)).then(inventories.count()))
                .expectNext(2L).expectComplete().verify(TIMEOUT);
    }

    @Test
    void existingInventoryAndSupplyAreMigratedIntoBothLanguages() throws Exception {
        Product embedded = Product.builder().productName("Embedded supply")
                .productDescription("Embedded description").build();
        Inventory inventory = Inventory.builder().inventoryId("legacy-inventory").inventoryName("Legacy inventory")
                .inventoryDescription("Legacy description").products(List.of(embedded)).build();
        Product supply = Product.builder().productId("legacy-supply").inventoryId("legacy-inventory")
                .productName("Legacy supply").productDescription("Legacy supply description").build();

        StepVerifier.create(inventories.insert(inventory).then(products.insert(supply)))
                .expectNextCount(1).expectComplete().verify(TIMEOUT);
        loader.run();

        StepVerifier.create(inventories.findInventoryByInventoryId("legacy-inventory"))
                .assertNext(saved -> {
                    assertThat(saved.getName()).isEqualTo(Map.of("en", "Legacy inventory", "fr", "Legacy inventory"));
                    assertThat(saved.getDescription()).isEqualTo(Map.of("en", "Legacy description", "fr", "Legacy description"));
                    assertThat(saved.getProducts().get(0).getName())
                            .isEqualTo(Map.of("en", "Embedded supply", "fr", "Embedded supply"));
                })
                .expectComplete().verify(TIMEOUT);
        StepVerifier.create(products.findProductByProductId("legacy-supply"))
                .assertNext(saved -> {
                    assertThat(saved.getName()).isEqualTo(Map.of("en", "Legacy supply", "fr", "Legacy supply"));
                    assertThat(saved.getDescription())
                            .isEqualTo(Map.of("en", "Legacy supply description", "fr", "Legacy supply description"));
                })
                .expectComplete().verify(TIMEOUT);
    }

    @Test
    void existingStarterDataReceivesFrenchTranslationsWithoutOverwritingStaffText() throws Exception {
        Product embedded = Product.builder().productName("Carprofen")
                .productDescription("Non-steroidal anti-inflammatory drug").build();
        Inventory starter = Inventory.builder().inventoryId("starter").inventoryName("Medications")
                .inventoryDescription("Antibiotics for pet infections").products(List.of(embedded)).build();
        Inventory customized = Inventory.builder().inventoryId("customized").inventoryName("First-Aid")
                .name(Map.of("en", "Staff kit", "fr", "Trousse personnalisée"))
                .inventoryDescription("Custom description").build();
        Product supply = Product.builder().productId("starter-supply").inventoryId("starter")
                .productName("Carprofen").productDescription("Non-steroidal anti-inflammatory drug").build();
        Product customizedSupply = Product.builder().productId("customized-supply").inventoryId("starter")
                .productName("Gauze Pads").name(Map.of("en", "Staff gauze", "fr", "Gaze personnalisée"))
                .productDescription("Custom supply description").build();

        StepVerifier.create(inventories.insert(starter).then(inventories.insert(customized))
                        .then(products.insert(supply)).then(products.insert(customizedSupply)))
                .expectNextCount(1).expectComplete().verify(TIMEOUT);

        loader.run();
        loader.run();

        StepVerifier.create(inventories.findInventoryByInventoryId("starter"))
                .assertNext(saved -> {
                    assertThat(saved.getName()).isEqualTo(Map.of("en", "Medications", "fr", "Médicaments"));
                    assertThat(saved.getDescription()).isEqualTo(Map.of("en", "Antibiotics for pet infections",
                            "fr", "Antibiotiques pour traiter les infections chez les animaux"));
                    assertThat(saved.getProducts().get(0).getName())
                            .isEqualTo(Map.of("en", "Carprofen", "fr", "Carprofène"));
                    assertThat(saved.getProducts().get(0).getDescription())
                            .isEqualTo(Map.of("en", "Non-steroidal anti-inflammatory drug",
                                    "fr", "Anti-inflammatoire non stéroïdien"));
                }).expectComplete().verify(TIMEOUT);
        StepVerifier.create(products.findProductByProductId("starter-supply"))
                .assertNext(saved -> {
                    assertThat(saved.getName()).isEqualTo(Map.of("en", "Carprofen", "fr", "Carprofène"));
                    assertThat(saved.getDescription()).isEqualTo(Map.of("en", "Non-steroidal anti-inflammatory drug",
                            "fr", "Anti-inflammatoire non stéroïdien"));
                })
                .expectComplete().verify(TIMEOUT);
        StepVerifier.create(inventories.findInventoryByInventoryId("customized"))
                .assertNext(saved -> assertThat(saved.getName())
                        .isEqualTo(Map.of("en", "Staff kit", "fr", "Trousse personnalisée")))
                .expectComplete().verify(TIMEOUT);
        StepVerifier.create(products.findProductByProductId("customized-supply"))
                .assertNext(saved -> assertThat(saved.getName())
                        .isEqualTo(Map.of("en", "Staff gauze", "fr", "Gaze personnalisée")))
                .expectComplete().verify(TIMEOUT);
    }

    @Test
    void conflictingLegacyNamesAllowStartupWithoutRemovingRecords() throws Exception {
        Inventory first = Inventory.builder().inventoryId("first").inventoryName("Gauze")
                .inventoryDescription("First description").build();
        Inventory second = Inventory.builder().inventoryId("second").inventoryName("GAUZE")
                .inventoryDescription("Second description").build();
        StepVerifier.create(mongoTemplate.indexOps(Inventory.class).dropIndex("inventory_name_en_unique")
                        .then(mongoTemplate.indexOps(Inventory.class).dropIndex("inventory_name_fr_unique")))
                .expectComplete().verify(TIMEOUT);
        try {
            StepVerifier.create(inventories.insert(first).then(inventories.insert(second)))
                    .expectNextCount(1).expectComplete().verify(TIMEOUT);

            loader.run();

            StepVerifier.create(inventories.findAll().collectList())
                    .assertNext(saved -> {
                        assertThat(saved).extracting(Inventory::getInventoryId)
                                .containsExactlyInAnyOrder("first", "second");
                        assertThat(saved).allSatisfy(inventory ->
                                assertThat(inventory.getName().get("en")).isEqualTo(inventory.getInventoryName()));
                    }).expectComplete().verify(TIMEOUT);
        } finally {
            inventories.deleteAll().then(localizedNameIndexes.ensureIndexes()).block(TIMEOUT);
        }
    }

    private void assertOneWriteSucceeds(Mono<?> first, Mono<?> second) {
        Flux<Boolean> outcomes = Flux.merge(
                first.thenReturn(true).onErrorResume(DuplicateKeyException.class, error -> Mono.just(false)),
                second.thenReturn(true).onErrorResume(DuplicateKeyException.class, error -> Mono.just(false)));

        StepVerifier.create(outcomes.collectList())
                .assertNext(results -> assertThat(results).containsExactlyInAnyOrder(true, false))
                .expectComplete().verify(TIMEOUT);
    }

    private Map<String, String> conflictingName(String language) {
        return "en".equals(language)
                ? Map.of("en", "GAUZE", "fr", "Autre article")
                : Map.of("en", "Other supply", "fr", "GAZE");
    }
}
