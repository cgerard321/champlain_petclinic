package com.petclinic.bffapigateway.presentationlayer.v1.Customers;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.CustomerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.CustomerResponseDTO;
import com.petclinic.bffapigateway.dtos.Files.FileDetails;
import com.petclinic.bffapigateway.dtos.Pets.PetRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.presentationlayer.v1.CustomerControllerV1;
import com.petclinic.bffapigateway.presentationlayer.v1.CustomersLookupController;
import com.petclinic.bffapigateway.presentationlayer.v1.PetControllerV1;
import com.petclinic.bffapigateway.utils.Security.Filters.CsrfFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.IsUserFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.JwtTokenFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.RoleFilter;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@WebFluxTest(
        controllers = {
                CustomerControllerV1.class,
                CustomersLookupController.class,
                PetControllerV1.class
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtTokenFilter.class, RoleFilter.class, IsUserFilter.class, CsrfFilter.class}
        )
)
@AutoConfigureWebTestClient
public class CustomersControllerV1UnitTests {

    @Autowired
    private WebTestClient client;

    @MockBean
    private CustomersServiceClient customersServiceClient;

    String customerId = "customerId-123";
    @Test
    void whenGetAllCustomers_thenReturnCustomers() {
        CustomerResponseDTO customer = new CustomerResponseDTO();
        customer.setCustomerId("customerId-90");
        customer.setFirstName("John");
        customer.setLastName("Johnny");

        when(customersServiceClient.getAllCustomers()).thenReturn(Flux.just(customer));

        client.get()
                .uri("/api/gateway/customers")
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerResponseDTO.class)
                .value(list -> {
                    assertNotNull(list);
                    assertEquals(1, list.size());
                    assertEquals("customerId-90", list.get(0).getCustomerId());
                });
    }

    @Test
    void whenGetAllCustomersByPagination_thenReturnCustomers() {
        CustomerResponseDTO customerResponseDTO = new CustomerResponseDTO();
        customerResponseDTO.setCustomerId("customerId-09");
        customerResponseDTO.setFirstName("Test");
        customerResponseDTO.setLastName("Test");
        customerResponseDTO.setAddress("Test");
        customerResponseDTO.setCity("Test");
        customerResponseDTO.setProvince("Test");
        customerResponseDTO.setTelephone("Test");

        Optional<Integer> page = Optional.of(0);
        Optional<Integer> size = Optional.of(1);

        when(customersServiceClient.getCustomersByPagination(page, size, null, null, null, null, null))
                .thenReturn(Flux.just(customerResponseDTO));

        client.get()
                .uri("/api/gateway/customers/customers-pagination?page=0&size=1")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/event-stream;charset=UTF-8")
                .expectBodyList(CustomerResponseDTO.class)
                .value(list -> {
                    assertNotNull(list);
                    assertEquals(1, list.size());
                    assertEquals("customerId-09", list.get(0).getCustomerId());
                });
    }

    @Test
    void whenGetAllCustomersByPagination_withEmptyPageAndSize_thenReturnEmptyList() {
        when(customersServiceClient.getCustomersByPagination(null, null, null, null, null, null, null))
                .thenReturn(Flux.empty());

        client.get()
                .uri("/api/gateway/customers/customers-pagination")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/event-stream;charset=UTF-8")
                .expectBodyList(CustomerResponseDTO.class)
                .value(list -> assertEquals(0, list.size()));
    }

    @Test
    void whenGetTotalNumberOfCustomers_thenReturnCount() {
        long expectedCount = 0L;
        when(customersServiceClient.getTotalNumberOfCustomers()).thenReturn(Mono.just(expectedCount));

        client.get()
                .uri("/api/gateway/customers/customers-count")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .value(body -> assertEquals(expectedCount, body));
    }

    @Test
    void whenGetTotalNumberOfCustomers_WithFilters_thenReturnCount() {
        long expectedCount = 0L;
        when(customersServiceClient.getTotalNumberOfCustomersWithFilters(null, null, null, null, null))
                .thenReturn(Mono.just(expectedCount));

        client.get()
                .uri("/api/gateway/customers/customers-filtered-count")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .value(body -> assertEquals(expectedCount, body));
    }

    @Test
    void whenGetCustomerByCustomerId_thenReturnCustomer() {
        CustomerResponseDTO customerResponseDTO = new CustomerResponseDTO();
        customerResponseDTO.setCustomerId("customerId-123");
        customerResponseDTO.setFirstName("John");
        customerResponseDTO.setLastName("Johnny");
        customerResponseDTO.setAddress("111 John St");
        customerResponseDTO.setCity("Johnston");
        customerResponseDTO.setProvince("Quebec");
        customerResponseDTO.setTelephone("51451545144");

        when(customersServiceClient.getCustomer("customerId-123", false))
                .thenReturn(Mono.just(customerResponseDTO));

        client.get()
                .uri("/api/gateway/customers/detail/{customerId}", customerResponseDTO.getCustomerId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CustomerResponseDTO.class)
                .value(customerResponseDTO1 -> {
                    assertNotNull(customerResponseDTO1);
                    assertEquals(customerResponseDTO1.getCustomerId(), customerResponseDTO.getCustomerId());
                });
    }

    @Test
    void whenUpdateCustomer_thenReturnUpdatedCustomer() {
        String customerId = "f470653d-05c5-4c45-b7a0-7d70f003d2ac";
        CustomerRequestDTO updatedCustomer = new CustomerRequestDTO();
        updatedCustomer.setFirstName("UpdatedFirstName");
        updatedCustomer.setLastName("UpdatedLastName");

        CustomerResponseDTO customerResponseDTO = new CustomerResponseDTO();
        customerResponseDTO.setCustomerId(customerId);
        customerResponseDTO.setFirstName(updatedCustomer.getFirstName());
        customerResponseDTO.setLastName(updatedCustomer.getLastName());

        when(customersServiceClient.updateCustomer(eq(customerId), any()))
                .thenReturn(Mono.just(customerResponseDTO));

        client.put()
                .uri("/api/gateway/customers/" + customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(updatedCustomer))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CustomerResponseDTO.class)
                .isEqualTo(customerResponseDTO);

        Mockito.verify(customersServiceClient, times(1))
                .updateCustomer(eq(customerId), any());
    }

    @Test
    void whenDeleteCustomer_thenReturnNoContent() {

        when(customersServiceClient.deleteCustomer(customerId)).thenReturn(Mono.empty());

        client.delete()
                .uri("/api/gateway/customers/{customerId}", customerId)
                .exchange()
                .expectStatus().isNoContent();

        verify(customersServiceClient, times(1)).deleteCustomer(customerId);
    }

    @Test
    void whenGetPetsByCustomerId_thenReturnListOfPets() {
        String customerId = "customerId-123";

        PetResponseDTO pet1 = new PetResponseDTO();
        pet1.setName("Rocky");
        PetResponseDTO pet2 = new PetResponseDTO();
        pet2.setName("Bella");

        when(customersServiceClient.getPetsByCustomerId(customerId)).thenReturn(Flux.just(pet1, pet2));

        client.get()
                .uri("/api/gateway/pets/customers/{customerId}/pets", customerId)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(PetResponseDTO.class)
                .hasSize(2)
                .value(list -> assertEquals("Rocky", list.get(0).getName()));

        verify(customersServiceClient, times(1)).getPetsByCustomerId(customerId);
    }

    @Test
    void whenGetCustomerWithPhoto_thenReturnCustomerWithPhotoData() {
        CustomerResponseDTO customer = new CustomerResponseDTO();
        customer.setCustomerId(customerId);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        FileDetails photo = new FileDetails();
        photo.setFileData("mockPhotoData".getBytes());
        photo.setFileType("image/png");
        customer.setPhoto(photo);

        when(customersServiceClient.getCustomer(customerId, true))
                .thenReturn(Mono.just(customer));

        client.get()
                .uri("/api/gateway/customers/detail/{customerId}?includePhoto=true", customerId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponseDTO.class)
                .value(customerResponseDTO -> {
                    assertEquals(customerId, customerResponseDTO.getCustomerId());
                    assertEquals("John", customerResponseDTO.getFirstName());
                    assertEquals("Doe", customerResponseDTO.getLastName());
                    assertNotNull(customerResponseDTO.getPhoto());
                    assertArrayEquals("mockPhotoData".getBytes(), customerResponseDTO.getPhoto().getFileData());
                    assertEquals("image/png", customerResponseDTO.getPhoto().getFileType());
                });

        verify(customersServiceClient, times(1)).getCustomer(customerId, true);
    }

    @Test
    void whenGetCustomerWithoutPhoto_thenReturnCustomerWithoutPhotoData() {
        CustomerResponseDTO customerResponseDTO = new CustomerResponseDTO();
        customerResponseDTO.setCustomerId(customerId);
        customerResponseDTO.setFirstName("John");
        customerResponseDTO.setLastName("Doe");

        when(customersServiceClient.getCustomer(customerId, false))
                .thenReturn(Mono.just(customerResponseDTO));

        client.get()
                .uri("/api/gateway/customers/detail/{customerId}?includePhoto=false", customerId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponseDTO.class)
                .value(customerResponseDTO1 -> {
                    assertEquals(customerId, customerResponseDTO1.getCustomerId());
                    assertEquals("John", customerResponseDTO1.getFirstName());
                    assertEquals("Doe", customerResponseDTO1.getLastName());
                    assertNull(customerResponseDTO1.getPhoto());
                });

        verify(customersServiceClient, times(1)).getCustomer(customerId, false);
    }

    @Test
    void whenGetCustomerWithDefaultPhoto_thenReturnCustomerWithDefaultPhoto() {
        CustomerResponseDTO customer = new CustomerResponseDTO();
        customer.setCustomerId(customerId);
        customer.setFirstName("John");
        customer.setLastName("Doe");

        when(customersServiceClient.getCustomer(customerId, false))
                .thenReturn(Mono.just(customer));

        client.get()
                .uri("/api/gateway/customers/detail/{customerId}", customerId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponseDTO.class)
                .value(customerResponseDTO -> {
                    assertEquals(customerId, customerResponseDTO.getCustomerId());
                    assertEquals("John", customerResponseDTO.getFirstName());
                    assertEquals("Doe", customerResponseDTO.getLastName());
                });

        verify(customersServiceClient, times(1)).getCustomer(customerId, false);
    }

    @Test
    void whenGetAllCustomersByPagination_withFilters_thenReturnFilteredCustomers() {
        CustomerResponseDTO customer = new CustomerResponseDTO();
        customer.setCustomerId("customer1");
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setCity("Montreal");
        customer.setTelephone("5551234567");

        Optional<Integer> page = Optional.of(0);
        Optional<Integer> size = Optional.of(5);

        when(customersServiceClient.getCustomersByPagination(page, size, "customer1", "John", "Doe", "5551234567", "Montreal"))
                .thenReturn(Flux.just(customer));

        client.get()
                .uri("/api/gateway/customers/customers-pagination?page=0&size=5&customerId=customer1&firstName=John&lastName=Doe&phoneNumber=5551234567&city=Montreal")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(CustomerResponseDTO.class)
                .value(list -> {
                    assertNotNull(list);
                    assertEquals(1, list.size());
                    assertEquals("customer1", list.get(0).getCustomerId());
                });
    }

    @Test
    void whenGetTotalNumberOfCustomersWithFilters_withFilters_thenReturnFilteredCount() {
        long expectedCount = 5L;

        when(customersServiceClient.getTotalNumberOfCustomersWithFilters("customer1", "John", "Doe", "5551234567", "Montreal"))
                .thenReturn(Mono.just(expectedCount));

        client.get()
                .uri("/api/gateway/customers/customers-filtered-count?customerId=customer1&firstName=John&lastName=Doe&phoneNumber=5551234567&city=Montreal")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .value(body -> assertEquals(expectedCount, body));
    }

    @Test
    void whenGetCustomerByCustomerId_withNonExistentCustomer_thenReturnNotFound() {
        when(customersServiceClient.getCustomer("nonexistent", false))
                .thenReturn(Mono.empty());

        client.get()
                .uri("/api/gateway/customers/{customerId}", "nonexistent")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenUpdateCustomer_withNonExistentCustomer_thenReturnNotFound() {
        CustomerRequestDTO requestDTO = new CustomerRequestDTO();
        requestDTO.setFirstName("John");
        requestDTO.setLastName("Doe");

        when(customersServiceClient.updateCustomer(eq("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a"), any()))
                .thenReturn(Mono.empty());

        client.put()
                .uri("/api/gateway/customers/{customerId}", "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestDTO))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenGetPetsByCustomerId_withNoPets_thenReturnEmptyList() {
        String customerId = "customerId-123";
        when(customersServiceClient.getPetsByCustomerId(customerId))
                .thenReturn(Flux.empty());

        client.get()
                .uri("/api/gateway/pets/customers/{customerId}/pets", customerId)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(PetResponseDTO.class)
                .hasSize(0);
    }

    @Test
    void whenUpdateCustomerPhoto_withValidPhoto_thenReturnUpdatedCustomer() {
        FileDetails photoRequest = FileDetails.builder()
                .fileName("profile.jpeg")
                .fileType("image/jpeg")
                .fileData("mockPhotoData".getBytes())
                .build();

        CustomerResponseDTO updatedCustomer = new CustomerResponseDTO();
        updatedCustomer.setCustomerId(customerId);
        updatedCustomer.setFirstName("John");
        updatedCustomer.setLastName("Doe");
        FileDetails photo = new FileDetails();
        photo.setFileData("mockPhotoData".getBytes());
        photo.setFileType("image/jpeg");
        updatedCustomer.setPhoto(photo);

        when(customersServiceClient.updateCustomerPhoto(eq(customerId), any()))
                .thenReturn(Mono.just(updatedCustomer));

        client.patch()
                .uri("/api/gateway/customers/{customerId}/photo", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(photoRequest))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CustomerResponseDTO.class)
                .value(body -> {
                    assertNotNull(body);
                    assertEquals(customerId, body.getCustomerId());
                    assertNotNull(body.getPhoto());
                    assertEquals("image/jpeg", body.getPhoto().getFileType());
                    assertArrayEquals("mockPhotoData".getBytes(), body.getPhoto().getFileData());
                });

        verify(customersServiceClient, times(1)).updateCustomerPhoto(eq(customerId), any());
    }

    @Test
    void whenUpdateCustomerPhoto_withNonExistentCustomer_thenReturnNotFound() {
        FileDetails photoRequest = FileDetails.builder()
                .fileName("profile.jpeg")
                .fileType("image/jpeg")
                .fileData("mockPhotoData".getBytes())
                .build();

        when(customersServiceClient.updateCustomerPhoto(eq("nonexistent"), any()))
                .thenReturn(Mono.empty());

        client.patch()
                .uri("/api/gateway/customers/{customerId}/photo", "nonexistent")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(photoRequest))
                .exchange()
                .expectStatus().isNotFound();

        verify(customersServiceClient, times(1)).updateCustomerPhoto(eq("nonexistent"), any());
    }

    @Test
    void whenUpdateCustomerPhoto_withPngPhoto_thenReturnUpdatedCustomer() {
        FileDetails photoRequest = FileDetails.builder()
                .fileName("avatar.png")
                .fileType("image/png")
                .fileData("pngPhotoData".getBytes())
                .build();

        CustomerResponseDTO updatedCustomer = new CustomerResponseDTO();
        updatedCustomer.setCustomerId(customerId);
        updatedCustomer.setFirstName("Jane");
        updatedCustomer.setLastName("Smith");
        FileDetails photo = new FileDetails();
        photo.setFileData("pngPhotoData".getBytes());
        photo.setFileType("image/png");
        updatedCustomer.setPhoto(photo);

        when(customersServiceClient.updateCustomerPhoto(eq(customerId), any()))
                .thenReturn(Mono.just(updatedCustomer));

        client.patch()
                .uri("/api/gateway/customers/{customerId}/photo", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(photoRequest))
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponseDTO.class)
                .value(body -> {
                    assertNotNull(body);
                    assertEquals(customerId, body.getCustomerId());
                    assertNotNull(body.getPhoto());
                    assertEquals("image/png", body.getPhoto().getFileType());
                });

        verify(customersServiceClient, times(1)).updateCustomerPhoto(eq(customerId), any());
    }

    @Test
    void whenDeleteCustomerPhoto_thenReturnOk() {
        CustomerResponseDTO responseWithoutPhoto = new CustomerResponseDTO();
        responseWithoutPhoto.setCustomerId(customerId);
        responseWithoutPhoto.setPhoto(null);

        when(customersServiceClient.deleteCustomerPhoto(customerId)).thenReturn(Mono.just(responseWithoutPhoto));

        client.delete()
                .uri("/api/gateway/customers/{customerId}/photo", customerId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponseDTO.class)
                .value(body -> {
                    assertEquals(customerId, body.getCustomerId());
                    assertNull(body.getPhoto());
                });

        verify(customersServiceClient, times(1)).deleteCustomerPhoto(customerId);
    }

    @Test
    void whenDeleteCustomerPhoto_withNonExistentCustomer_thenReturnNotFound() {
        when(customersServiceClient.deleteCustomerPhoto(customerId)).thenReturn(Mono.empty());

        client.delete()
                .uri("/api/gateway/customers/{customerId}/photo", customerId)
                .exchange()
                .expectStatus().isNotFound();
        verify(customersServiceClient, times(1)).deleteCustomerPhoto(customerId);
    }

    @Test
    void whenDeletePetPhoto_withValidPet_thenReturnOk() {
        String petId = "pet-id-123";
        PetResponseDTO petResponseDTO = new PetResponseDTO();
        petResponseDTO.setPetId(petId);
        petResponseDTO.setName("Test Pet");
        petResponseDTO.setPhoto(null);

        when(customersServiceClient.deletePetPhoto(petId)).thenReturn(Mono.just(petResponseDTO));

        client.patch()
                .uri("/api/gateway/pets/{petId}/photo", petId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PetResponseDTO.class)
                .value(body -> {
                    assertEquals(petId, body.getPetId());
                    assertEquals("Test Pet", body.getName());
                    assertNull(body.getPhoto());
                });

        verify(customersServiceClient, times(1)).deletePetPhoto(petId);
    }

    @Test
    void whenDeletePetPhoto_withNonExistentPet_thenReturnNotFound() {
        String petId = "non-existent-pet-id";
        
        when(customersServiceClient.deletePetPhoto(petId)).thenReturn(Mono.empty());

        client.patch()
                .uri("/api/gateway/pets/{petId}/photo", petId)
                .exchange()
                .expectStatus().isNotFound();

        verify(customersServiceClient, times(1)).deletePetPhoto(petId);
    }

    @Test
    void whenCreatePetForCustomer_withValidRequest_thenReturnCreated() {
        String customerId = "customerId-123";
        String petId = "pet-id-123";
        PetRequestDTO petRequest = new PetRequestDTO();
        petRequest.setName("New Pet");
        petRequest.setPetTypeId("pt-1");
        petRequest.setWeight("5.0");
        petRequest.setIsActive("true");

        PetResponseDTO createdPet = new PetResponseDTO();
        createdPet.setPetId(petId);
        createdPet.setName("New Pet");
        createdPet.setPetTypeId("pt-1");
        createdPet.setWeight("5.0");
        createdPet.setIsActive("true");

        when(customersServiceClient.createPetForCustomer(customerId, petRequest))
                .thenReturn(Mono.just(createdPet));

        client.post()
                .uri("/api/gateway/pets/customers/{customerId}/pets", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(petRequest))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PetResponseDTO.class)
                .value(body -> {
                    assertEquals(petId, body.getPetId());
                    assertEquals("New Pet", body.getName());
                });

        verify(customersServiceClient, times(1)).createPetForCustomer(customerId, petRequest);
    }

    @Test
    void whenCreatePetForCustomer_withInvalidRequest_thenReturnBadRequest() {
        String customerId = "customerId-123";
        PetRequestDTO petRequest = new PetRequestDTO();
        petRequest.setName("New Pet");

        when(customersServiceClient.createPetForCustomer(customerId, petRequest))
                .thenReturn(Mono.empty());

        client.post()
                .uri("/api/gateway/pets/customers/{customerId}/pets", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(petRequest))
                .exchange()
                .expectStatus().isBadRequest();

        verify(customersServiceClient, times(1)).createPetForCustomer(customerId, petRequest);
    }

    @Test
    void whenGetPet_withValidIds_thenReturnPet() {
        String customerId = "customerId-123";
        String petId = "pet-id-123";
        PetResponseDTO pet = new PetResponseDTO();
        pet.setPetId(petId);
        pet.setName("Test Pet");

        when(customersServiceClient.getPetByPetId(petId, false))
                .thenReturn(Mono.just(pet));

        client.get()
                .uri("/api/gateway/pets/customers/{customerId}/pets/{petId}", customerId, petId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PetResponseDTO.class)
                .value(body -> {
                    assertEquals(petId, body.getPetId());
                    assertEquals("Test Pet", body.getName());
                });

        verify(customersServiceClient, times(1)).getPetByPetId(petId, false);
    }

    @Test
    void whenGetPet_withNonExistentPet_thenReturnNotFound() {
        String customerId = "customerId-123";
        String petId = "non-existent-pet-id";

        when(customersServiceClient.getPetByPetId(petId, false))
                .thenReturn(Mono.empty());

        client.get()
                .uri("/api/gateway/pets/customers/{customerId}/pets/{petId}", customerId, petId)
                .exchange()
                .expectStatus().isNotFound();

        verify(customersServiceClient, times(1)).getPetByPetId(petId, false);
    }

    @Test
    void whenDeletePet_withValidId_thenReturnNoContent() {
        String petId = "pet-id-123";

        PetResponseDTO deletedPet = new PetResponseDTO();
        deletedPet.setPetId(petId);
        deletedPet.setName("Deleted Pet");

        when(customersServiceClient.deletePetByPetId(petId))
                .thenReturn(Mono.just(deletedPet));

        client.delete()
                .uri("/api/gateway/pets/{petId}", petId)
                .exchange()
                .expectStatus().isNoContent();

        verify(customersServiceClient, times(1)).deletePetByPetId(petId);
    }

    @Test
    void whenDeletePet_withEmptyServiceResponse_thenReturnNoContent() {
        String petId = "non-existent-pet-id";

        when(customersServiceClient.deletePetByPetId(petId))
                .thenReturn(Mono.empty());

        client.delete()
                .uri("/api/gateway/pets/{petId}", petId)
                .exchange()
                .expectStatus().isNoContent();

        verify(customersServiceClient, times(1)).deletePetByPetId(petId);
    }

    @Test
    void whenGetAllPets_thenReturnListOfPets() {
        PetResponseDTO pet1 = new PetResponseDTO();
        pet1.setPetId("pet-1");
        pet1.setName("Fluffy");
        
        PetResponseDTO pet2 = new PetResponseDTO();
        pet2.setPetId("pet-2");
        pet2.setName("Buddy");

        when(customersServiceClient.getAllPets()).thenReturn(Flux.just(pet1, pet2));

        client.get()
                .uri("/api/gateway/pets")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/event-stream;charset=UTF-8")
                .expectBodyList(PetResponseDTO.class)
                .hasSize(2)
                .value(list -> {
                    assertEquals("pet-1", list.get(0).getPetId());
                    assertEquals("Fluffy", list.get(0).getName());
                    assertEquals("pet-2", list.get(1).getPetId());
                    assertEquals("Buddy", list.get(1).getName());
                });

        verify(customersServiceClient, times(1)).getAllPets();
    }

    @Test
    void whenPatchPet_withValidRequest_thenReturnOk() {
        String petId = "pet-id-123";
        String isActive = "false";
        
        PetResponseDTO updatedPet = new PetResponseDTO();
        updatedPet.setPetId(petId);
        updatedPet.setName("Updated Pet");
        updatedPet.setIsActive(isActive);

        when(customersServiceClient.patchPet(isActive, petId))
                .thenReturn(Mono.just(updatedPet));

        client.patch()
                .uri("/api/gateway/pets/{petId}/active?isActive={isActive}", petId, isActive)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PetResponseDTO.class)
                .value(body -> {
                    assertEquals(petId, body.getPetId());
                    assertEquals("Updated Pet", body.getName());
                    assertEquals(isActive, body.getIsActive());
                });

        verify(customersServiceClient, times(1)).patchPet(isActive, petId);
    }

    @Test
    void whenPatchPet_withInvalidRequest_thenReturnBadRequest() {
        String petId = "pet-id-123";
        String isActive = "invalid";

        when(customersServiceClient.patchPet(isActive, petId))
                .thenReturn(Mono.empty());

        client.patch()
                .uri("/api/gateway/pets/{petId}/active?isActive={isActive}", petId, isActive)
                .exchange()
                .expectStatus().isBadRequest();

        verify(customersServiceClient, times(1)).patchPet(isActive, petId);
    }
}
