package com.petclinic.inventoryservice.businesslayer;

import com.petclinic.inventoryservice.datalayer.Audit.SupplyAuditRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryTypeRepository;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import com.petclinic.inventoryservice.presentationlayer.InventoryRequestDTO;
import com.petclinic.inventoryservice.presentationlayer.ProductRequestDTO;
import com.petclinic.inventoryservice.utils.InventoryValidator;
import com.petclinic.inventoryservice.utils.SupplyReportPdfGenerator;
import com.petclinic.inventoryservice.utils.exceptions.UnprocessableEntityException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocalizedInventoryServiceTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final Map<String, String> NAMES = Map.of("en", "Bandages", "fr", "Pansements");
    private static final Map<String, String> DESCRIPTIONS = Map.of("en", "Wound care", "fr", "Soins des plaies");

    private final InventoryRepository inventories = mock(InventoryRepository.class);
    private final ProductRepository products = mock(ProductRepository.class);
    private final InventoryTypeRepository types = mock(InventoryTypeRepository.class);
    private final ProductInventoryServiceImpl service = new ProductInventoryServiceImpl(
            inventories, products, types, new InventoryValidator(inventories, types, products),
            mock(org.springframework.data.mongodb.core.ReactiveMongoTemplate.class),
            mock(SupplyAuditRepository.class), new SupplyReportPdfGenerator());

    static Stream<Map<String, String>> duplicateNames() {
        return Stream.of(Map.of("en", "bandages", "fr", "Unique"),
                Map.of("en", "Unique", "fr", "pansements"));
    }

    @ParameterizedTest
    @MethodSource("duplicateNames")
    void creatingInventoryRejectsDuplicateInEitherLanguage(Map<String, String> names) {
        when(inventories.count()).thenReturn(Mono.just(1L));
        when(inventories.findAll()).thenReturn(Flux.just(inventory("other")));

        StepVerifier.create(service.addInventory(Mono.just(inventoryRequest(names))))
                .expectError(UnprocessableEntityException.class).verify(TIMEOUT);
        verify(inventories, never()).insert(any(Inventory.class));
    }

    @ParameterizedTest
    @MethodSource("duplicateNames")
    void editingInventoryRejectsAnotherInventoryNameInEitherLanguage(Map<String, String> names) {
        when(inventories.findInventoryByInventoryId("current")).thenReturn(Mono.just(inventory("current")));
        when(inventories.findAll()).thenReturn(Flux.just(inventory("other")));

        StepVerifier.create(service.updateInventory(Mono.just(inventoryRequest(names)), "current"))
                .expectError(UnprocessableEntityException.class).verify(TIMEOUT);
        verify(inventories, never()).save(any(Inventory.class));
    }

    @ParameterizedTest
    @MethodSource("duplicateNames")
    void editingSupplyRejectsAnotherSupplyNameInEitherLanguage(Map<String, String> names) {
        when(inventories.findInventoryByInventoryId("inv1")).thenReturn(Mono.just(inventory("inv1")));
        when(products.findProductByProductId("current")).thenReturn(Mono.just(supply("current")));
        when(products.findAllProductsByInventoryId("inv1")).thenReturn(Flux.just(supply("other")));

        StepVerifier.create(service.updateProductInInventory(Mono.just(supplyRequest(names)), "inv1", "current", "manager"))
                .expectError(UnprocessableEntityException.class).verify(TIMEOUT);
        verify(products, never()).save(any(Product.class));
    }

    @Test
    void editingInventoryKeepsItsOwnNamesAndSavesBothDescriptions() {
        Inventory current = inventory("current");
        when(inventories.findInventoryByInventoryId("current")).thenReturn(Mono.just(current));
        when(inventories.findAll()).thenReturn(Flux.just(current));
        when(inventories.existsByInventoryNameAndInventoryIdNot("Bandages", "current")).thenReturn(Mono.just(false));
        when(inventories.save(any(Inventory.class))).thenAnswer(call -> Mono.just(call.getArgument(0)));

        StepVerifier.create(service.updateInventory(Mono.just(inventoryRequest(NAMES)), "current"))
                .assertNext(response -> {
                    assertEquals(NAMES, response.getName());
                    assertEquals(DESCRIPTIONS, response.getDescription());
                })
                .expectComplete().verify(TIMEOUT);
        verify(inventories).save(current);
    }

    @Test
    void editingSupplyKeepsItsOwnNamesAndSavesBothDescriptions() {
        Product current = supply("current");
        when(inventories.findInventoryByInventoryId("inv1")).thenReturn(Mono.just(inventory("inv1")));
        when(products.findProductByProductId("current")).thenReturn(Mono.just(current));
        when(products.findAllProductsByInventoryId("inv1")).thenReturn(Flux.just(current));
        when(products.existsByInventoryIdAndProductNameIgnoreCaseAndProductIdNot("inv1", "Bandages", "current"))
                .thenReturn(Mono.just(false));
        when(products.save(any(Product.class))).thenAnswer(call -> Mono.just(call.getArgument(0)));

        StepVerifier.create(service.updateProductInInventory(Mono.just(supplyRequest(NAMES)), "inv1", "current", "manager"))
                .assertNext(response -> {
                    assertEquals(NAMES, response.getName());
                    assertEquals(DESCRIPTIONS, response.getDescription());
                })
                .expectComplete().verify(TIMEOUT);
        verify(products).save(current);
    }

    @Test
    void inventoryNamesAreComparedWithinTheSameLanguage() {
        Map<String, String> reversed = Map.of("en", "Pansements", "fr", "Bandages");
        when(inventories.count()).thenReturn(Mono.just(1L));
        when(inventories.findAll()).thenReturn(Flux.just(inventory("other")));
        when(inventories.existsByInventoryName("Pansements")).thenReturn(Mono.just(false));
        when(inventories.insert(any(Inventory.class))).thenAnswer(call -> Mono.just(call.getArgument(0)));

        StepVerifier.create(service.addInventory(Mono.just(inventoryRequest(reversed))))
                .assertNext(response -> assertEquals(reversed, response.getName()))
                .expectComplete().verify(TIMEOUT);
    }

    @Test
    void inventoryCreateConflictAtDatabaseBecomesValidationError() {
        DuplicateKeyException conflict = new DuplicateKeyException("inventory_name_fr_unique");
        when(inventories.count()).thenReturn(Mono.just(0L));
        when(inventories.findAll()).thenReturn(Flux.empty());
        when(inventories.existsByInventoryName("Bandages")).thenReturn(Mono.just(false));
        when(inventories.insert(any(Inventory.class))).thenReturn(Mono.error(conflict));

        verifyConflict(service.addInventory(Mono.just(inventoryRequest(NAMES))), conflict);
    }

    @Test
    void inventoryUpdateConflictAtDatabaseBecomesValidationError() {
        DuplicateKeyException conflict = new DuplicateKeyException("inventory_name_en_unique");
        when(inventories.findInventoryByInventoryId("current")).thenReturn(Mono.just(inventory("current")));
        when(inventories.findAll()).thenReturn(Flux.empty());
        when(inventories.existsByInventoryNameAndInventoryIdNot("Bandages", "current"))
                .thenReturn(Mono.just(false));
        when(inventories.save(any(Inventory.class))).thenReturn(Mono.error(conflict));

        verifyConflict(service.updateInventory(Mono.just(inventoryRequest(NAMES)), "current"), conflict);
    }

    @Test
    void supplyCreateConflictAtDatabaseBecomesValidationError() {
        DuplicateKeyException conflict = new DuplicateKeyException("supply_name_fr_unique");
        when(inventories.findInventoryByInventoryId("inv1")).thenReturn(Mono.just(inventory("inv1")));
        when(products.existsByInventoryIdAndProductNameIgnoreCase("inv1", "Bandages"))
                .thenReturn(Mono.just(false));
        when(products.findAllProductsByInventoryId("inv1")).thenReturn(Flux.empty());
        when(products.save(any(Product.class))).thenReturn(Mono.error(conflict));

        verifyConflict(service.addSupplyToInventory(Mono.just(supplyRequest(NAMES)), "inv1", "manager"), conflict);
    }

    @Test
    void supplyUpdateConflictAtDatabaseBecomesValidationError() {
        DuplicateKeyException conflict = new DuplicateKeyException("supply_name_en_unique");
        when(inventories.findInventoryByInventoryId("inv1")).thenReturn(Mono.just(inventory("inv1")));
        when(products.findProductByProductId("current")).thenReturn(Mono.just(supply("current")));
        when(products.findAllProductsByInventoryId("inv1")).thenReturn(Flux.empty());
        when(products.existsByInventoryIdAndProductNameIgnoreCaseAndProductIdNot("inv1", "Bandages", "current"))
                .thenReturn(Mono.just(false));
        when(products.save(any(Product.class))).thenReturn(Mono.error(conflict));

        verifyConflict(service.updateProductInInventory(Mono.just(supplyRequest(NAMES)), "inv1", "current", "manager"),
                conflict);
    }

    @Test
    void movingSupplyIntoConflictingInventoryBecomesValidationError() {
        DuplicateKeyException conflict = new DuplicateKeyException("supply_name_fr_unique");
        when(products.findProductByInventoryIdAndProductId("inv1", "current"))
                .thenReturn(Mono.just(supply("current")));
        when(products.save(any(Product.class))).thenReturn(Mono.error(conflict));

        verifyConflict(service.updateProductInventoryId("inv1", "current", "inv2"), conflict);
    }

    private void verifyConflict(Mono<?> operation, DuplicateKeyException conflict) {
        StepVerifier.create(operation)
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(UnprocessableEntityException.class, error);
                    assertSame(conflict, error.getCause());
                })
                .verify(TIMEOUT);
    }

    private Inventory inventory(String id) {
        return Inventory.builder().inventoryId(id).inventoryName("Bandages").inventoryType("Equipment")
                .inventoryDescription("Wound care").name(NAMES).description(DESCRIPTIONS).build();
    }

    private Product supply(String id) {
        return Product.builder().productId(id).inventoryId("inv1").productName("Bandages")
                .productDescription("Wound care").name(NAMES).description(DESCRIPTIONS)
                .productQuantity(10).productPrice(2.0).productSalePrice(3.0).build();
    }

    private InventoryRequestDTO inventoryRequest(Map<String, String> names) {
        return InventoryRequestDTO.builder().inventoryType("Equipment").name(names).description(DESCRIPTIONS).build();
    }

    private ProductRequestDTO supplyRequest(Map<String, String> names) {
        return ProductRequestDTO.builder().name(names).description(DESCRIPTIONS)
                .productQuantity(10).productPrice(2.0).productSalePrice(3.0).build();
    }
}
