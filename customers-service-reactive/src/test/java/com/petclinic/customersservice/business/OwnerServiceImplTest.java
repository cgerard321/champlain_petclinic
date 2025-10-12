package com.petclinic.customersservice.business;

import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.data.OwnerRepo;
import com.petclinic.customersservice.domainclientlayer.FilesServiceClient;
import com.petclinic.customersservice.presentationlayer.OwnerRequestDTO;
import com.petclinic.customersservice.presentationlayer.OwnerResponseDTO;
import com.petclinic.customersservice.util.EntityDTOUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @MockBean
    private FilesServiceClient filesServiceClient;

    @Autowired
    private OwnerService ownerService;

    private final Owner ownerEntity = buildOwner();
    private final OwnerRequestDTO ownerRequestDTO = new OwnerRequestDTO();

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
                .build();
    }

    @Test
    void getAllOwners_ShouldSucceed() {
        // Arrange
        Owner ownerEntity = buildOwner();
        when(repo.findAll()).thenReturn(Flux.just(ownerEntity));

        // Act & Assert
        StepVerifier
                .create(ownerService.getAllOwners()) // Call service method
                .expectNextMatches(ownerDto -> ownerDto.getOwnerId().equals(ownerEntity.getOwnerId()))
                .expectComplete()
                .verify();
        verify(repo).findAll();
    }

    @Test
    void getOwnersPagination_ShouldSucceed(){
        // Arrange: Setup 3 owners
        Owner owner1 = buildOwner();
        owner1.setOwnerId("ownerId-11");
        Owner owner2 = buildOwner();
        owner2.setOwnerId("ownerId-17");
        Owner owner3 = buildOwner();
        owner3.setOwnerId("ownerId-13");

        Pageable pageable = PageRequest.of(0, 2);

        when(repo.findAll()).thenReturn(Flux.just(owner1, owner2, owner3));

        Flux<OwnerResponseDTO> owners = ownerService.getAllOwnersPagination(pageable,null,null,null,null,null);

        StepVerifier.create(owners)
                .expectNextMatches(ownerDto1 -> ownerDto1.getOwnerId().equals(owner1.getOwnerId()))
                .expectNextMatches(ownerDto2 -> ownerDto2.getOwnerId().equals(owner2.getOwnerId()))
                .expectComplete()
                .verify();
    }

    @Test
    void getOwnersPaginationWithFiltersApplied1_ShouldSucceed(){
        Owner owner1 = buildOwner();
        owner1.setOwnerId("ownerId-11");
        owner1.setCity("test city1");

        Pageable pageable = PageRequest.of(0, 2);
        String city = "test city1";
        String ownerId = "ownerId-11";

        when(repo.findAll()).thenReturn(Flux.just(owner1));

        Flux<OwnerResponseDTO> owners = ownerService.getAllOwnersPagination(pageable,ownerId,null,null,null,city);

        StepVerifier.create(owners)
                .expectNextMatches(ownerDto1 -> ownerDto1.getOwnerId().equals(owner1.getOwnerId())
                        && ownerDto1.getCity().equals(owner1.getCity()))
                .expectComplete()
                .verify();
    }

    @Test
    void getOwnersPaginationWithFiltersApplied2_ShouldSucceed(){
        Owner owner1 = buildOwner();
        owner1.setOwnerId("ownerId-2");
        owner1.setFirstName("FirstName2");
        owner1.setLastName("LastName2");
        owner1.setCity("test city2");
        owner1.setTelephone("telephone2");

        Pageable pageable = PageRequest.of(0, 2);

        String city = "test city2";
        String ownerId = "ownerId-2";
        String lastName = "LastName2";
        String firstName = "FirstName2";
        String phoneNumber = "telephone2";

        when(repo.findAll()).thenReturn(Flux.just(owner1));

        Flux<OwnerResponseDTO> owners = ownerService.getAllOwnersPagination(pageable,ownerId,firstName,lastName,phoneNumber,city);

        StepVerifier.create(owners)
                .expectNextMatches(
                        ownerDto1 -> ownerDto1.getOwnerId().equals(owner1.getOwnerId())
                                && ownerDto1.getCity().equals(owner1.getCity())
                                && ownerDto1.getTelephone().equals(owner1.getTelephone())
                                && ownerDto1.getFirstName().equals(owner1.getFirstName())
                                && ownerDto1.getLastName().equals(owner1.getLastName()))
                .expectComplete()
                .verify();
    }

    @Test
    void insertOwner_ShouldSucceed() {
        Owner ownerEntity = buildOwner();
        when(repo.insert(any(Owner.class))).thenReturn(Mono.just(ownerEntity));

        Mono<Owner> returnedOwner = ownerService.insertOwner(Mono.just(ownerEntity));

        StepVerifier.create(returnedOwner).consumeNextWith(foundOwner -> {
                    assertEquals(ownerEntity.getId(), foundOwner.getId());
                    assertEquals(ownerEntity.getOwnerId(), foundOwner.getOwnerId());
                })
                .verifyComplete();
        verify(repo).insert(any(Owner.class));
    }

    @Test
    void newInsertOwner_ShouldSucceed() {
        Owner ownerEntity = buildOwner();
        Mono<Owner> ownerMono = Mono.just(ownerEntity);
        when(repo.insert(any(Owner.class))).thenReturn(ownerMono);

        Mono<Owner> returnedOwner = ownerService.insertOwner(ownerMono);

        StepVerifier.create(returnedOwner)
                .consumeNextWith(foundOwner -> {
                    // Check using the business ID (ownerId)
                    assertEquals(ownerEntity.getOwnerId(), foundOwner.getOwnerId());
                })
                .verifyComplete();
        verify(repo).insert(any(Owner.class));
    }

    @Test
    void getOwnerByOwnerId_ShouldSucceed() {
        Owner ownerEntity = buildOwner();
        String OWNER_ID = ownerEntity.getOwnerId();
        when(repo.findOwnerByOwnerId(OWNER_ID)).thenReturn(Mono.just(ownerEntity));

        Mono<OwnerResponseDTO> ownerResponseDTOMono = ownerService.getOwnerByOwnerId(OWNER_ID);

        StepVerifier
                .create(ownerResponseDTOMono)
                .consumeNextWith(foundOwner -> {
                    assertEquals(ownerEntity.getOwnerId(), foundOwner.getOwnerId());
                })
                .verifyComplete();
        verify(repo).findOwnerByOwnerId(OWNER_ID);
    }

    @Test
    void deleteOwnerByOwnerId_ShouldCompleteSuccessfully() {
        String OWNER_ID = ownerEntity.getOwnerId();
        when(repo.deleteById(OWNER_ID)).thenReturn(Mono.empty());

        Mono<Void> deleteObj = ownerService.deleteOwner(OWNER_ID);

        StepVerifier
                .create(deleteObj)
                .verifyComplete();

       verify(repo).deleteById(OWNER_ID);

    }


    @Test
    void updateOwner_ShouldSucceed() {
        // Define input data
        String ownerId = "ownerId-123";
        OwnerRequestDTO ownerRequestDTO = new OwnerRequestDTO();
        ownerRequestDTO.setFirstName("Updated First Name");

        Owner existingOwner = buildOwner();

        when(repo.findOwnerByOwnerId(ownerId)).thenReturn(Mono.just(existingOwner));
        when(repo.save(any(Owner.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Mono<OwnerResponseDTO> updatedOwnerMono = ownerService.updateOwner(Mono.just(ownerRequestDTO), ownerId);

        StepVerifier.create(updatedOwnerMono)
                .expectNextMatches(updatedOwner -> {
                    assertEquals(ownerId, updatedOwner.getOwnerId());
                    assertEquals(ownerRequestDTO.getFirstName(), updatedOwner.getFirstName());
                    return true;
                })
                .expectComplete()
                .verify();

        verify(repo).findOwnerByOwnerId(ownerId);
        verify(repo).save(any(Owner.class));
    }

    @Test
    void getOwnerByOwnerId_ShouldThrowNotFoundException() {
        String OWNER_ID = "Not found";
        when(repo.findOwnerByOwnerId(OWNER_ID)).thenReturn(Mono.empty());

        Mono<OwnerResponseDTO> ownerResponseDTOMono = ownerService.getOwnerByOwnerId(OWNER_ID);
        StepVerifier
                .create(ownerResponseDTOMono)
                .expectError(NotFoundException.class)
                .verify();
        verify(repo).findOwnerByOwnerId(OWNER_ID);
    }

}
