package com.petclinic.inventoryservice.datalayer.Inventory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class InventoryRepositoryTest {
    @Autowired
    InventoryRepository inventoryRepository;



    @Test
    public void shouldSaveSingleInventory(){
        //arrange
        Inventory newInventory = buildInventory("inventoryId_3", "inventoryType_3", InventoryType.internal, "inventoryDescription_3");
        Publisher<Inventory> setup = inventoryRepository.save(newInventory);
        //act and assert
        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();
    }



    @Test public void findInventoryByInventoryID_shouldFindOne(){


        Inventory newInventory = buildInventory("inventoryId_3", "internal", InventoryType.internal ,"inventoryDescription_3");

        StepVerifier
                .create(inventoryRepository.findInventoryByInventoryId(newInventory.getInventoryId()))
                .assertNext(inventory -> {
                    assertThat(inventory.getInventoryId()).isEqualTo(newInventory.getInventoryId());
                    assertThat(newInventory.getInventoryName()).isEqualTo(newInventory.getInventoryName());
                });
    }


    @Test
    public void shouldDeleteInventory() {
        // Arrange
        Inventory inventoryToBeDeleted = buildInventory("inventoryId_4", "inventoryType_4", InventoryType.internal ,"inventoryDescription_4");
        inventoryRepository.save(inventoryToBeDeleted).block();

        // Act
        inventoryRepository.deleteById(inventoryToBeDeleted.getInventoryId()).block();

        // Assert
        StepVerifier
                .create(inventoryRepository.findById(inventoryToBeDeleted.getInventoryId()))
                .expectNextCount(0)  // No inventory should be found
                .verifyComplete();
    }
    private Inventory buildInventory(String inventoryId, String inventoryName, InventoryType inventoryType, String inventoryDescription) {
        return Inventory.builder()
                .inventoryName(inventoryName)
                .inventoryId(inventoryId)
                .inventoryType(inventoryType)
                .inventoryDescription(inventoryDescription)
                .build();
    }



}