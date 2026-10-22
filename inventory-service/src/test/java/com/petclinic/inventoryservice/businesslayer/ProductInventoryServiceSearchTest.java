package com.petclinic.inventoryservice.businesslayer;

import com.petclinic.inventoryservice.datalayer.Audit.SupplyAuditRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryTypeRepository;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import com.petclinic.inventoryservice.presentationlayer.InventoryResponseDTO;
import com.petclinic.inventoryservice.utils.InventoryValidator;
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
            mock(InventoryValidator.class), mock(org.springframework.data.mongodb.core.ReactiveMongoTemplate.class),
            mock(SupplyAuditRepository.class));

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
        when(inventoryRepository.findAllByInventoryType("Internal")).thenReturn(Flux.just(
                inventory("none", null), inventory("text", "INV-ABC"), inventory("two", "INV-0002"), inventory("one", "INV-0001")));

        StepVerifier.create(service.searchInventories(PageRequest.of(0, 10), null, null, "Internal", null, null)
                        .map(InventoryResponseDTO::getInventoryId))
                .expectNext("one", "two", "text", "none")
                .verifyComplete();
    }

    @Test
    void searchInventories_byTypeAndDescription_shouldReturnMatches() {
        when(inventoryRepository.findAllByInventoryTypeAndInventoryDescription("Internal", "Tools"))
                .thenReturn(Flux.just(inventory("one", "INV-0001")));

        StepVerifier.create(service.searchInventories(PageRequest.of(0, 10), null, null, "Internal", "Tools", null))
                .expectNextCount(1)
                .verifyComplete();
    }
}
