package com.petclinic.inventoryservice.datalayer.Inventory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.core.publisher.Flux;
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
        Inventory newInventory = buildInventory(
                "inventoryId_3",
                "inventoryType_3",
                "Internal",
                "inventoryDescription_3",
                "https://www.fda.gov/files/iStock-157317886.jpg",
                "https://www.who.int/images/default-source/wpro/countries/viet-nam/health-topics/vaccines.jpg?sfvrsn=89a81d7f_14"

        );
        Publisher<Inventory> setup = inventoryRepository.save(newInventory);
        //act and assert
        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();
    }



    @Test public void findInventoryByInventoryID_shouldFindOne(){


        Inventory newInventory = buildInventory(
                "inventoryId_3",
                "internal",
                "Internal" ,
                "inventoryDescription_3",
                "https://www.fda.gov/files/iStock-157317886.jpg",
                "https://www.who.int/images/default-source/wpro/countries/viet-nam/health-topics/vaccines.jpg?sfvrsn=89a81d7f_14"

        );

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
        Inventory inventoryToBeDeleted = buildInventory(
                "inventoryId_4",
                "inventoryType_4",
                "Internal" ,
                "inventoryDescription_4",
                "https://www.fda.gov/files/iStock-157317886.jpg",
                "https://www.who.int/images/default-source/wpro/countries/viet-nam/health-topics/vaccines.jpg?sfvrsn=89a81d7f_14"

        );
        inventoryRepository.save(inventoryToBeDeleted).block();

        // Act
        inventoryRepository.deleteById(inventoryToBeDeleted.getInventoryId()).block();

        // Assert
        StepVerifier
                .create(inventoryRepository.findById(inventoryToBeDeleted.getInventoryId()))
                .expectNextCount(0)  // No inventory should be found
                .verifyComplete();
    }
    //search
    @Test
    public void shouldFindInventoryByNameTypeAndDescription() {
        // Arrange
        Inventory inventory = buildInventory(
                "inventoryId_1",
                "SampleName",
                "Internal",
                "SampleDescription",
                "https://www.fda.gov/files/iStock-157317886.jpg",
                "https://www.who.int/images/default-source/wpro/countries/viet-nam/health-topics/vaccines.jpg?sfvrsn=89a81d7f_14"

        );
        inventoryRepository.save(inventory).block();

        // Act & Assert
        StepVerifier
                .create(inventoryRepository.findAllByInventoryNameAndInventoryTypeAndInventoryDescription(
                        "SampleName",
                        "Internal",
                        "SampleDescription"
                ))
                .expectNextMatches(result -> result.getInventoryId().equals("inventoryId_1"))
                .verifyComplete();
    }

    @Test
    public void shouldFindInventoryByTypeAndDescription() {
        // Arrange
        Inventory inventory = buildInventory("inventoryId_2",
                "OtherName",
                "Internal",
                "SampleDescription",
                "https://www.fda.gov/files/iStock-157317886.jpg",
                "https://www.who.int/images/default-source/wpro/countries/viet-nam/health-topics/vaccines.jpg?sfvrsn=89a81d7f_14"

        );
        inventoryRepository.save(inventory).block();

        // Act & Assert
        StepVerifier
                .create(inventoryRepository.findAllByInventoryTypeAndInventoryDescription("Internal",
                        "SampleDescription"
                ))
                .expectNextMatches(result -> result.getInventoryId().equals("inventoryId_2"))
                .verifyComplete();
    }

    @Test
    public void shouldFindInventoryByName() {
        // Arrange
        Inventory inventory = buildInventory("inventoryId_3",
                "SampleName",
                "Internal",
                "OtherDescription",
                "https://www.fda.gov/files/iStock-157317886.jpg",
                "https://www.who.int/images/default-source/wpro/countries/viet-nam/health-topics/vaccines.jpg?sfvrsn=89a81d7f_14"

        );
        inventoryRepository.save(inventory).block();

        // Act & Assert
        StepVerifier
                .create(inventoryRepository.findAllByInventoryName("SampleName"))
                .expectNextMatches(result -> result.getInventoryId().equals("inventoryId_3"))
                .verifyComplete();
    }



    @Test
    public void shouldFindInventoryByNameRegex() {
        // Arrange
        Inventory inventory = buildInventory(
                "inventoryId_4",
                "Benzodiazepines",
                "Internal",
                "SomeDescription",
                "https://www.fda.gov/files/iStock-157317886.jpg",
                "https://www.who.int/images/default-source/wpro/countries/viet-nam/health-topics/vaccines.jpg?sfvrsn=89a81d7f_14"

        );
        inventoryRepository.save(inventory).block();

        String regexPattern = "(?i)^B.*";  // Just for this example since we know the name starts with 'B'

        // Act & Assert
        StepVerifier
                .create(inventoryRepository.findByInventoryNameRegex(regexPattern))
                .expectNextMatches(result -> result.getInventoryId().equals("inventoryId_4"))
                .verifyComplete();
    }

    @Test
    public void shouldFindInventoryByDescriptionRegex() {
        // Arrange
        Inventory inventory = buildInventory(
                "inventoryId_5",
                "SomeName",
                "Internal",
                "Medication",
                "https://www.fda.gov/files/iStock-157317886.jpg",
                "https://www.who.int/images/default-source/wpro/countries/viet-nam/health-topics/vaccines.jpg?sfvrsn=89a81d7f_14"

        );
        inventoryRepository.save(inventory).block();

        String regexPattern = "(?i)^M.*";  // Since we know the description starts with 'M'

        // Act & Assert
        StepVerifier
                .create(inventoryRepository.findByInventoryDescriptionRegex(regexPattern))
                .expectNextMatches(result -> result.getInventoryId().equals("inventoryId_5"))
                .verifyComplete();
    }

    private Inventory buildInventory(String inventoryId, String inventoryName, String inventoryType, String inventoryDescription, String inventoryImage, String inventoryBackupImage) {
        return Inventory.builder()
                .inventoryName(inventoryName)
                .inventoryId(inventoryId)
                .inventoryType(inventoryType)
                .inventoryDescription(inventoryDescription)
                .inventoryImage(inventoryImage)
                .inventoryBackupImage(inventoryBackupImage)
                .build();
    }



}