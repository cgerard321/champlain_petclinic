package com.petclinic.inventoryservice.datalayer.Inventory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;

import java.util.UUID;

@DataMongoTest
class InventoryRepositoryTest {
    @Autowired
    InventoryRepository inventoryRepository;



    @Test
    public void shouldSaveSingleInventory(){
        //arrange
        Inventory newInventory = buildInventory("inventoryId_3", "inventory_name" , InventoryType.internal, "inventoryDescription_3");
        Publisher<Inventory> setup = inventoryRepository.save(newInventory);
        //act and assert
        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();
    }


    private Inventory buildInventory(String inventoryId, String inventoryName, InventoryType inventoryType, String inventoryDescription) {
        return Inventory.builder()
                .inventoryId(inventoryId)
                .name(inventoryName)
                .inventoryType(inventoryType)
                .inventoryDescription(inventoryDescription)
                .build();
    }



}