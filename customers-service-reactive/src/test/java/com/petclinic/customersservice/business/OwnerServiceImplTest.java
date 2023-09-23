package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.OwnerRepo;
import com.petclinic.customersservice.presentationlayer.OwnerRequestDTO;
import com.petclinic.customersservice.presentationlayer.OwnerResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port= 27020"})
@AutoConfigureWebTestClient
class OwnerServiceImplTest {

    @MockBean
    private OwnerRepo repo;

    @Autowired
    private OwnerService ownerService;


    @Test
    void getAllOwners_ShouldSucceed() {
        OwnerResponseDTO ownerResponseDTO = OwnerResponseDTO.builder()
                .ownerId("ownerId-123")
                .firstName("FirstName")
                .lastName("LastName")
                .address("Test address")
                .city("test city")
                .telephone("telephone")
                .build();

        List<OwnerResponseDTO> owners = new ArrayList<>();
        owners.add(ownerResponseDTO);

        Flux<OwnerResponseDTO> returnAllOwners = Flux.just(ownerResponseDTO);

        StepVerifier
                .create(returnAllOwners)
                .expectNextMatches(ownerDto -> ownerDto.getOwnerId().equals(ownerResponseDTO.getOwnerId()))
                .expectComplete()
                .verify();
    }

    @Test
    void insertOwner() {
        Owner ownerEntity = buildOwner();
        Mono<Owner> ownerMono = Mono.just(ownerEntity);
        when(repo.insert(any(Owner.class))).thenReturn(ownerMono);
        Mono<Owner> returnedOwner = ownerService.insertOwner(Mono.just(ownerEntity));
        StepVerifier.create(returnedOwner).consumeNextWith(foundOwner -> {
            assertEquals(ownerEntity.getId(), foundOwner.getId());
            assertEquals(ownerEntity.getFirstName(), foundOwner.getFirstName());
            assertEquals(ownerEntity.getLastName(), foundOwner.getLastName());
            assertEquals(ownerEntity.getAddress(), foundOwner.getAddress());
            assertEquals(ownerEntity.getCity(), foundOwner.getCity());
            assertEquals(ownerEntity.getTelephone(), foundOwner.getTelephone());
            //assertEquals(ownerEntity.getPhotoId(), foundOwner.getPhotoId());
        })
                .verifyComplete();
    }

    // This new test uses ownerEntity.getOwnerId() rather than getId()
    @Test
    void newInsertOwner() {
        Owner ownerEntity = buildOwner();
        Mono<Owner> ownerMono = Mono.just(ownerEntity);
        when(repo.insert(any(Owner.class))).thenReturn(ownerMono);
        Mono<Owner> returnedOwner = ownerService.insertOwner(ownerMono);
        StepVerifier.create(returnedOwner)
                .consumeNextWith(foundOwner -> {
                    assertEquals(ownerEntity.getOwnerId(), foundOwner.getOwnerId());
                    assertEquals(ownerEntity.getFirstName(), foundOwner.getFirstName());
                    assertEquals(ownerEntity.getLastName(), foundOwner.getLastName());
                    assertEquals(ownerEntity.getAddress(), foundOwner.getAddress());
                    assertEquals(ownerEntity.getCity(), foundOwner.getCity());
                    assertEquals(ownerEntity.getTelephone(), foundOwner.getTelephone());
                })
                .verifyComplete();
    }

     @Test
     void getOwnerByOwnerId() {
        Owner ownerEntity = buildOwner();
        String OWNER_ID = ownerEntity.getOwnerId();
        when(repo.findOwnerByOwnerId(OWNER_ID)).thenReturn(Mono.just(ownerEntity));
        Mono<OwnerResponseDTO> ownerResponseDTOMono = ownerService.getOwnerByOwnerId(OWNER_ID);
        StepVerifier
                .create(ownerResponseDTOMono)
                .consumeNextWith(foundOwner -> {
                    assertEquals(ownerEntity.getOwnerId(), foundOwner.getOwnerId());
                    assertEquals(ownerEntity.getFirstName(), foundOwner.getFirstName());
                    assertEquals(ownerEntity.getLastName(), foundOwner.getLastName());
                    assertEquals(ownerEntity.getAddress(), foundOwner.getAddress());
                    assertEquals(ownerEntity.getCity(), foundOwner.getCity());
                    assertEquals(ownerEntity.getTelephone(), foundOwner.getTelephone());
                    //assertEquals(ownerEntity.getPhotoId(), foundOwner.getPhotoId());
                })
                .verifyComplete();
     }

     @Test
     void getOwnerByOwnerIdNotFound() {

         Owner ownerEntity = buildOwner();
         String OWNER_ID = "Not found";
         when(repo.findOwnerByOwnerId(OWNER_ID)).thenReturn(Mono.just(ownerEntity));
         Mono<OwnerResponseDTO> ownerResponseDTOMono = ownerService.getOwnerByOwnerId(OWNER_ID);
         StepVerifier
                 .create(ownerResponseDTOMono)
                 .expectNextCount(1)
                 .expectError();

     }

     @Test
     void deleteOwnerByOwnerId() {
        Owner ownerEntity = buildOwner();
        String OWNER_ID = ownerEntity.getId();
        when(repo.deleteById(anyString())).thenReturn(Mono.empty());
        Mono<Void> deleteObj = ownerService.deleteOwner(OWNER_ID);
        StepVerifier
                .create(deleteObj)
                .expectNextCount(0)
                .verifyComplete();
     }
    @Test
    public void deleteOwnerNotFound() {

        Owner owner = buildOwner();
        String OWNER_ID = "00";
        when(repo.deleteById(anyString())).thenReturn(Mono.empty());
        Mono<Void> ownerDelete = ownerService.deleteOwner(OWNER_ID);

        StepVerifier
                .create(ownerDelete)
                .expectNextCount(1)
                .expectError();
    }
    private Owner buildOwner() {
        return Owner.builder()
                .id("55")
                .ownerId("ownerId-123")
                .firstName("FirstName")
                .lastName("LastName")
                .address("Test address")
                .city("test city")
                .telephone("telephone")
                //.photoId("1")
                .build();
    }



    @Test
    void updateOwner_ShouldSucceed() {
        // Define input data
        String ownerId = "ownerId-123";
        OwnerRequestDTO ownerRequestDTO = new OwnerRequestDTO();
        ownerRequestDTO.setFirstName("Updated First Name");
        ownerRequestDTO.setLastName("Updated Last Name");
        ownerRequestDTO.setAddress("Updated Address");
        ownerRequestDTO.setCity("Updated City");
        ownerRequestDTO.setTelephone("5555555555");

        // Create a mock for an existing owner in the repository
        Owner existingOwner = new Owner();
        existingOwner.setOwnerId(ownerId);
        existingOwner.setFirstName("Original First Name");
        existingOwner.setLastName("Original Last Name");
        existingOwner.setAddress("Original Address");
        existingOwner.setCity("Original City");
        existingOwner.setTelephone("1234567890");

        // Mock the repository behavior
        when(repo.findOwnerByOwnerId(ownerId)).thenReturn(Mono.just(existingOwner));
        when(repo.save(any(Owner.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0))); // Return the saved owner

        // Invoke the service method
        Mono<OwnerResponseDTO> updatedOwnerMono = ownerService.updateOwner(Mono.just(ownerRequestDTO), ownerId);

        // Verify the result
        StepVerifier.create(updatedOwnerMono)
                .expectNextMatches(updatedOwner -> {
                    assertEquals(ownerId, updatedOwner.getOwnerId());
                    assertEquals(ownerRequestDTO.getFirstName(), updatedOwner.getFirstName());
                    assertEquals(ownerRequestDTO.getLastName(), updatedOwner.getLastName());
                    assertEquals(ownerRequestDTO.getAddress(), updatedOwner.getAddress());
                    assertEquals(ownerRequestDTO.getCity(), updatedOwner.getCity());
                    assertEquals(ownerRequestDTO.getTelephone(), updatedOwner.getTelephone());
                    return true;
                })
                .expectComplete()
                .verify();

        // Verify that the repository methods were called as expected
        verify(repo).findOwnerByOwnerId(ownerId);
        verify(repo).save(existingOwner);
    }

}
