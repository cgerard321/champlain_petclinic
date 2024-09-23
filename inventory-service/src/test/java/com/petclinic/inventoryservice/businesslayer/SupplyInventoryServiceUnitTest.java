package com.petclinic.inventoryservice.businesslayer;

import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Supply.Status;
import com.petclinic.inventoryservice.datalayer.Supply.Supply;
import com.petclinic.inventoryservice.presentationlayer.InventoryResponseDTO;
import com.petclinic.inventoryservice.presentationlayer.SupplyRequestDTO;
import com.petclinic.inventoryservice.presentationlayer.SupplyResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class SupplyInventoryServiceUnitTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private SupplyInventoryServiceImpl supplyInventoryService;

    private Inventory testInventory;
    private SupplyRequestDTO testSupplyRequestDTO;
    private Supply testSupply;

    @BeforeEach
    public void setup() {

        testInventory = Inventory.builder()
                .inventoryId("test-inventory-id")
                .inventoryName("TestInventory")
                .build();

        testInventory.setSupplies(new ArrayList<>());

        testSupplyRequestDTO = new SupplyRequestDTO(
                "TestSupply",
                "TestDescription",
                10.00,
                100,
                120.0
        );

        testSupply = new Supply(
                UUID.randomUUID().toString(),
                testInventory.getInventoryId(),
                "TestSupply",
                "TestDescription",
                10,
                100.0,
                120.0,
                Status.AVAILABLE
        );
    }


    @Test
    public void testAddSupplyToInventoryByInventoryName() {

        Mono<SupplyRequestDTO> supplyMono = Mono.just(testSupplyRequestDTO);

        System.out.println("Initial Inventory Supplies: " + testInventory.getSupplies());

        when(inventoryRepository.findByInventoryName("TestInventory"))
                .thenReturn(Mono.just(testInventory));

        when(inventoryRepository.save(any(Inventory.class)))
                .thenAnswer(invocation -> {
                    Inventory inventoryToSave = invocation.getArgument(0);
                    if (inventoryToSave.getSupplies().isEmpty()) {
                        Supply supplyToAdd = new Supply(
                                UUID.randomUUID().toString(),
                                inventoryToSave.getInventoryId(),
                                testSupplyRequestDTO.getSupplyName(),
                                testSupplyRequestDTO.getSupplyDescription(),
                                testSupplyRequestDTO.getSupplyQuantity(),
                                testSupplyRequestDTO.getSupplyPrice(),
                                testSupplyRequestDTO.getSupplySalePrice(),
                                Status.AVAILABLE
                        );
                        inventoryToSave.addSupply(supplyToAdd);
                    }
                    return Mono.just(inventoryToSave);
                });

        Mono<InventoryResponseDTO> result = supplyInventoryService
                .addSupplyToInventoryByInventoryName("TestInventory", supplyMono);

        StepVerifier.create(result)
                .consumeNextWith(inventoryResponseDTO -> {
                    System.out.println("Final Inventory Supplies: " + inventoryResponseDTO.getSupplies());
                    assertEquals(1, inventoryResponseDTO.getSupplies().size());
                    assertEquals("TestSupply", inventoryResponseDTO.getSupplies().get(0).getSupplyName());
                })
                .verifyComplete();

        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    public void testAddSupplyToInventoryByInventoryName_InventoryNotFound() {
        Mono<SupplyRequestDTO> supplyMono = Mono.just(testSupplyRequestDTO);

        when(inventoryRepository.findByInventoryName("TestInventory"))
                .thenReturn(Mono.empty());

        Mono<InventoryResponseDTO> result = supplyInventoryService
                .addSupplyToInventoryByInventoryName("TestInventory", supplyMono);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertEquals("No inventory found for name: TestInventory", throwable.getMessage());
                })
                .verify();
    }

    @Test
    public void testGetSuppliesByInventoryName() {

        Supply supply1 = new Supply(
                "id1",
                "test-inventory-id",
                "Supply1",
                "Description1",
                10,
                100.0,
                120.0,
                Status.AVAILABLE
        );

        Supply supply2 = new Supply(
                "id2",
                "test-inventory-id",
                "Supply2",
                "Description2",
                15,
                200.0,
                250.0,
                Status.OUT_OF_STOCK
        );

        testInventory.setSupplies(new ArrayList<>(Arrays.asList(supply1, supply2)));

        when(inventoryRepository.findByInventoryName("TestInventory"))
                .thenReturn(Mono.just(testInventory));

        Flux<SupplyResponseDTO> result = supplyInventoryService.getSuppliesByInventoryName("TestInventory");

        StepVerifier.create(result)
                .expectNextMatches(supply -> supply.getSupplyName().equals("Supply1"))
                .expectNextMatches(supply -> supply.getSupplyName().equals("Supply2"))
                .verifyComplete();
    }

    @Test
    public void testGetSuppliesByInventoryName_InventoryNotFound() {

        when(inventoryRepository.findByInventoryName("TestInventory"))
                .thenReturn(Mono.empty());

        Flux<SupplyResponseDTO> result = supplyInventoryService.getSuppliesByInventoryName("TestInventory");

        StepVerifier.create(result)
                .expectComplete()
                .verify();
    }


}
