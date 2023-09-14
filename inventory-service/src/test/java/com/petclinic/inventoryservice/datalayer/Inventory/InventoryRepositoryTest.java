package com.petclinic.inventoryservice.datalayer.Inventory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;

@DataMongoTest
class InventoryRepositoryUnitTest {
    @Autowired
    InventoryRepository inventoryRepository;

    Inventory inventory1;
    Inventory inventory2;
    @BeforeEach
    public void setupDB(){
        inventory1 = buildInventory("inventoryId_1", "inventoryType_1", "inventoryDescription_1");
        Publisher<Inventory> setup1 = inventoryRepository.deleteAll()
                .thenMany(inventoryRepository.save(inventory1));
        StepVerifier
                .create(setup1)
                .expectNextCount(1)
                .verifyComplete();
        inventory2 = buildInventory("inventoryId_2", "inventoryType_2", "inventoryDescription_2");
        Publisher<Inventory> setup2 = inventoryRepository.save(inventory2)
                .thenMany(inventoryRepository.save(inventory2));
        StepVerifier
                .create(setup2)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void shouldSaveSingleInventory(){
        //arrange
        Inventory newInventory = buildInventory("inventoryId_3", "inventoryType_3", "inventoryDescription_3");
        Publisher<Inventory> setup = inventoryRepository.save(newInventory);
        //act and assert
        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();
    }


    private Inventory buildInventory(String inventoryId, String inventoryType, String inventoryDescription) {
        return Inventory.builder()
                .inventoryId(inventoryId)
                .inventoryType(inventoryType)
                .inventoryDescription(inventoryDescription)
                .build();
    }



}