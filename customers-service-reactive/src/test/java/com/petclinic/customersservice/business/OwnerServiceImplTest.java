package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.OwnerRepo;
import com.petclinic.customersservice.presentationlayer.OwnerRequestDTO;
import com.petclinic.customersservice.presentationlayer.OwnerResponseDTO;
import com.petclinic.customersservice.util.EntityDTOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
                .province("test province")
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
    void getOwnersPagination_ShouldSucceed(){

        Owner owner1 = Owner.builder()
                .ownerId("ownerId-11")
                .firstName("FirstName1")
                .lastName("LastName1")
                .address("Test address1")
                .city("test city1")
                .province("test province1")
                .telephone("telephone1")
                .build();
        Owner owner2 = Owner.builder()
                .ownerId("ownerId-17")
                .firstName("FirstName2")
                .lastName("LastName2")
                .address("Test address2")
                .city("test city2")
                .province("test province2")
                .telephone("telephone2")
                .build();
        Owner owner3 = Owner.builder()
                .ownerId("ownerId-13")
                .firstName("FirstName3")
                .lastName("LastName3")
                .address("Test address3")
                .city("test city3")
                .province("test province3")
                .telephone("telephone3")
                .build();

        // Create a Pageable object for the first page with 2 items per page
        Pageable pageable = PageRequest.of(0, 2);

        // Mock the repository to return a Flux of owners
        when(repo.findAll()).thenReturn(Flux.just(owner1, owner2, owner3));

        // Call the method under test
        Flux<OwnerResponseDTO> owners = ownerService.getAllOwnersPagination(pageable,null,null,null,null,null);

        // Verify the behavior using StepVerifier
        StepVerifier.create(owners)
                .expectNextMatches(ownerDto1 -> ownerDto1.getOwnerId().equals(owner1.getOwnerId()))
                .expectNextMatches(ownerDto2 -> ownerDto2.getOwnerId().equals(owner2.getOwnerId()))
                .expectComplete()
                .verify();

    }

    @Test
    void getOwnersPaginationWithFiltersApplied_ShouldSucceed(){

        Owner owner1 = Owner.builder()
                .ownerId("ownerId-11")
                .firstName("FirstName1")
                .lastName("LastName1")
                .address("Test address1")
                .city("test city1")
                .province("test province1")
                .telephone("telephone1")
                .build();

        // Create a Pageable object for the first page with 2 items per page
        Pageable pageable = PageRequest.of(0, 2);
        String city = "test city1";
        String ownerId = "ownerId-11";
        // Mock the repository to return a Flux of owners
        when(repo.findAll()).thenReturn(Flux.just(owner1));

        // Call the method under test
        Flux<OwnerResponseDTO> owners = ownerService.getAllOwnersPagination(pageable,ownerId,null,null,null,city);

        // Verify the behavior using StepVerifier
        StepVerifier.create(owners)
                .expectNextMatches(ownerDto1 -> ownerDto1.getOwnerId().equals(owner1.getOwnerId())
                                            && ownerDto1.getCity().equals(owner1.getCity()))
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
            assertEquals(ownerEntity.getProvince(), foundOwner.getProvince());
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
                    assertEquals(ownerEntity.getProvince(), foundOwner.getProvince());
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
                    assertEquals(ownerEntity.getProvince(), foundOwner.getProvince());
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
                .province("test province")
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
        ownerRequestDTO.setProvince("Updated Province");
        ownerRequestDTO.setTelephone("5555555555");

        // Create a mock for an existing owner in the repository
        Owner existingOwner = new Owner();
        existingOwner.setOwnerId(ownerId);
        existingOwner.setFirstName("Original First Name");
        existingOwner.setLastName("Original Last Name");
        existingOwner.setAddress("Original Address");
        existingOwner.setCity("Original City");
        existingOwner.setProvince("Original Province");
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
                    assertEquals(ownerRequestDTO.getProvince(), updatedOwner.getProvince());
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
