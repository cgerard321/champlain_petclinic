package com.petclinic.customersservice.business;

import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.data.Pet;
import com.petclinic.customersservice.data.PetRepo;
import com.petclinic.customersservice.presentationlayer.PetRequestDTO;
import com.petclinic.customersservice.presentationlayer.PetResponseDTO;
import com.petclinic.customersservice.presentationlayer.CustomerResponseDTO;
import com.petclinic.customersservice.domainclientlayer.FilesServiceClient;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PetServiceUnitTest {

    @Mock
    private PetRepo repo;

    @Mock
    private CustomerService customerService;

    @Mock
    private FilesServiceClient filesServiceClient;

    @InjectMocks
    private PetServiceImpl petService;


    @Test
    void whenCreatePetForCustomer_withValidCustomerAndPetRequest_thenReturnPetResponseDTO() {
        String customerId = "valid-customer-id";
        PetRequestDTO petRequest = buildPetRequestDTO();
        Pet savedPet = buildPetFromRequest(petRequest, customerId);
        CustomerResponseDTO customerResponse = buildCustomerResponseDTO();

        when(customerService.getCustomerByCustomerId(customerId, false)).thenReturn(Mono.just(customerResponse));
        when(repo.save(any(Pet.class))).thenReturn(Mono.just(savedPet));

        Mono<PetResponseDTO> result = petService.createPetForCustomer(customerId, Mono.just(petRequest));

        StepVerifier
                .create(result)
                .consumeNextWith(createdPet -> {
                    assertEquals(savedPet.getName(), createdPet.getName());
                    assertEquals(savedPet.getPetTypeId(), createdPet.getPetTypeId());
                    assertEquals(savedPet.getCustomerId(), createdPet.getCustomerId());
                    assertEquals(savedPet.getWeight(), createdPet.getWeight());
                    assertEquals("true", createdPet.getIsActive());
                })
                .verifyComplete();
    }

    @Test
    void whenCreatePetForCustomer_withNonExistingCustomer_thenReturnNotFoundException() {
        String nonExistingCustomerId = "non-existent-customer-id";
        PetRequestDTO petRequest = buildPetRequestDTO();

        when(customerService.getCustomerByCustomerId(nonExistingCustomerId, false)).thenReturn(Mono.empty());

        Mono<PetResponseDTO> result = petService.createPetForCustomer(nonExistingCustomerId, Mono.just(petRequest));

        StepVerifier
                .create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("Customer not found with id: " + nonExistingCustomerId))
                .verify();
    }

    private Pet buildPet() {
        return Pet.builder()
                .petId("a-very-valid-pet-id")
                .name("Cookie")
                .customerId("a-very-valid-customer-id")
                .petTypeId("1")
                .birthDate(new Date())
                .isActive("true")
                .build();
    }

    private PetRequestDTO buildPetRequestDTO() {
        return PetRequestDTO.builder()
                .name("Buddy")
                .petTypeId("2")
                .birthDate(new Date())
                .weight("15.5")
                .isActive("true")
                .build();
    }

    private Pet buildPetFromRequest(PetRequestDTO request, String customerId) {
        return Pet.builder()
                .petId("generated-pet-id")
                .name(request.getName())
                .customerId(customerId)
                .petTypeId(request.getPetTypeId())
                .birthDate(request.getBirthDate())
                .weight(request.getWeight())
                .isActive("true")
                .build();
    }

    private CustomerResponseDTO buildCustomerResponseDTO() {
        return CustomerResponseDTO.builder()
                .customerId("valid-customer-id")
                .firstName("John")
                .lastName("Doe")
                .address("123 Main St")
                .city("Test City")
                .province("Test Province")
                .telephone("555-1234")
                .build();
    }
}