package com.petclinic.inventoryservice.businesslayer;

import com.petclinic.inventoryservice.datalayer.Audit.SupplyAuditRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryTypeRepository;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import com.petclinic.inventoryservice.presentationlayer.InventoryResponseDTO;
import com.petclinic.inventoryservice.utils.InventoryValidator;
import com.petclinic.inventoryservice.utils.SupplyReportPdfGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Plain Mockito tests (no Spring context or embedded Mongo) for the inventory search paths
// that ProductInventoryServiceUnitTest does not reach.
class ProductInventoryServiceSearchTest {

    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final InventoryRepository inventoryRepository = mock(InventoryRepository.class);
    private final ProductInventoryServiceImpl service = new ProductInventoryServiceImpl(
            inventoryRepository, productRepository, mock(InventoryTypeRepository.class),
            mock(InventoryValidator.class),
            mock(org.springframework.data.mongodb.core.ReactiveMongoTemplate.class),
            mock(SupplyAuditRepository.class), new SupplyReportPdfGenerator());
    @BeforeEach
    void setUp() {
        when(productRepository.countByInventoryIdAndLastUpdatedAtAfter(anyString(), any(LocalDateTime.class)))
                .thenReturn(Mono.just(0L));
    }

    private static Inventory inventory(String id, String code) {
        return Inventory.builder().inventoryId(id).inventoryCode(code).inventoryType("Internal").build();
    }

    @Test
    void searchInventories_byType_shouldSortByCodeWithTextAndMissingCodesLast() {
        when(inventoryRepository.findAll()).thenReturn(Flux.just(
                inventory("none", null), inventory("text", "INV-ABC"), inventory("two", "INV-0002"), inventory("one", "INV-0001")));

        StepVerifier.create(service.searchInventories(PageRequest.of(0, 10), null, null, "Internal", null, null)
                        .map(InventoryResponseDTO::getInventoryId))
                .expectNext("one", "two", "text", "none")
                .verifyComplete();
    }

    @Test
    void searchInventories_byTypeAndDescription_shouldReturnMatches() {
        Inventory match = inventory("one", "INV-0001");
        match.setInventoryDescription("Tools and supplies");
        Inventory wrongDescription = inventory("two", "INV-0002");
        wrongDescription.setInventoryDescription("Medical supplies");
        when(inventoryRepository.findAll()).thenReturn(Flux.just(match, wrongDescription));

        StepVerifier.create(service.searchInventories(PageRequest.of(0, 10), null, null, "Internal", "Tools", null))
                .expectNextMatches(result -> "one".equals(result.getInventoryId()))
                .verifyComplete();
    }

    @Test
    void searchInventories_withNameTypeDescriptionAndFavorite_shouldApplyEveryFilter() {
        Inventory matching = Inventory.builder()
                .inventoryId("matching")
                .inventoryCode("INV-0001")
                .inventoryName("Medical Equipment")
                .inventoryType("Equipment")
                .inventoryDescription("Tools for surgery")
                .important(true)
                .build();
        Inventory wrongName = Inventory.builder()
                .inventoryId("wrong-name")
                .inventoryCode("INV-0002")
                .inventoryName("Surgical Equipment")
                .inventoryType("Equipment")
                .inventoryDescription("Tools for surgery")
                .important(true)
                .build();
        Inventory wrongType = Inventory.builder()
                .inventoryId("wrong-type")
                .inventoryCode("INV-0003")
                .inventoryName("Medical Equipment")
                .inventoryType("Bandages")
                .inventoryDescription("Tools for surgery")
                .important(true)
                .build();
        Inventory wrongDescription = Inventory.builder()
                .inventoryId("wrong-description")
                .inventoryCode("INV-0004")
                .inventoryName("Medical Equipment")
                .inventoryType("Equipment")
                .inventoryDescription("First aid supplies")
                .important(true)
                .build();
        Inventory notFavorite = Inventory.builder()
                .inventoryId("not-favorite")
                .inventoryCode("INV-0005")
                .inventoryName("Medical Equipment")
                .inventoryType("Equipment")
                .inventoryDescription("Tools for surgery")
                .important(false)
                .build();
        when(inventoryRepository.findAll()).thenReturn(Flux.just(
                wrongName, wrongType, wrongDescription, notFavorite, matching));

        StepVerifier.create(service.searchInventories(
                        PageRequest.of(0, 10), null, "medical", "Equipment", "tools", true)
                        .map(InventoryResponseDTO::getInventoryId))
                .expectNext("matching")
                .verifyComplete();
    }

    @Test
    void searchInventories_withMultipleFilters_shouldApplyPaginationAfterFiltering() {
        Inventory first = Inventory.builder()
                .inventoryId("first")
                .inventoryCode("INV-0001")
                .inventoryName("Medical Equipment")
                .inventoryType("Equipment")
                .inventoryDescription("Tools for surgery")
                .build();
        Inventory second = Inventory.builder()
                .inventoryId("second")
                .inventoryCode("INV-0002")
                .inventoryName("Medical Supplies")
                .inventoryType("Equipment")
                .inventoryDescription("Tools for treatment")
                .build();
        Inventory excluded = Inventory.builder()
                .inventoryId("excluded")
                .inventoryCode("INV-0003")
                .inventoryName("Medical Supplies")
                .inventoryType("Bandages")
                .inventoryDescription("Tools for treatment")
                .build();
        when(inventoryRepository.findAll()).thenReturn(Flux.just(second, excluded, first));

        StepVerifier.create(service.searchInventories(
                        PageRequest.of(1, 1), null, "Medical", "Equipment", "Tools", null)
                        .map(InventoryResponseDTO::getInventoryId))
                .expectNext("second")
                .verifyComplete();
    }
}
