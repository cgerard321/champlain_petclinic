package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.*;
import com.petclinic.bffapigateway.dtos.Vets.PhotoDetails;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static reactor.core.publisher.Mono.just;

public class CustomerServiceClientIntegrationTest {

    private CustomersServiceClient customersServiceClient;

    private MockWebServer server;

    private ObjectMapper mapper;

    final String test = "Test photo";
    final byte[] testBytes = test.getBytes();

    private final PhotoDetails TEST_PHOTO = PhotoDetails.builder()
            .id(2)
            .name("photo")
            .type("jpeg")
            .photo("testBytes")
            .build();

    private final OwnerRequestDTO TEST_OWNER = OwnerRequestDTO.builder()
            .ownerId("ownerId-123")
            .firstName("John")
            .lastName("Smith")
            .address("456 Elm")
            .city("Montreal")
            .province("QC")
            .telephone("5553334444")
            //.imageId(1)
            .build();

    private final PetTypeRequestDTO TEST_PETTYPE = PetTypeRequestDTO.builder()
            .name("Dog")
            .petTypeDescription("Mammal")
            .build();


    private final OwnerResponseDTO TEST_OWNER_RESPONSE = OwnerResponseDTO.builder()
            .ownerId("ownerId-123")
            .firstName("John")
            .lastName("Smith")
            .address("456 Elm")
            .city("Montreal")
            .province("QC")
            .telephone("5553334444")
            //.imageId(1)
            .build();

    private final PetTypeResponseDTO TEST_PETTYPE_RESPONSE = PetTypeResponseDTO.builder()
            .petTypeId("petTypeId-123")
            .name("Dog")
            .petTypeDescription("Mammal")
            .build();
    PetType type = new PetType();

    Date date = new Date(20221010);
    private final PetResponseDTO TEST_PET = PetResponseDTO.builder()
            .ownerId("ownerId-123")
            .petId("petId-123")
            .name("Cat")
            .birthDate(date)
            .petTypeId("5")
          //  .photoId("2")
            .isActive("true")
            .build();


    @BeforeEach
    void setUp(){

        server = new MockWebServer();
        customersServiceClient = new CustomersServiceClient(
                WebClient.builder(),
                server.getHostName(),
                String.valueOf(server.getPort())
        );
        mapper = new ObjectMapper();
    }

    @AfterEach
    void shutdown() throws IOException {
        this.server.shutdown();
    }

    //TODO
    /*@Test
    void createOwnerPhoto() throws JsonProcessingException {

        customersServiceClient.setOwnerPhoto(TEST_PHOTO, 1);
        final String body = mapper.writeValueAsString(mapper.convertValue(TEST_PHOTO, PhotoDetails.class));
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        final PhotoDetails testPhoto = customersServiceClient.getOwnerPhoto(2).block();

        assertEquals(TEST_PHOTO.getId(), testPhoto.getId());
        assertEquals(TEST_PHOTO.getName(), testPhoto.getName());
        assertEquals(TEST_PHOTO.getType(), testPhoto.getType());
//        assertEquals(TEST_PHOTO.getPhoto(), testPhoto.getPhoto());
    }*/

    @Test
    void insertOwner() throws JsonProcessingException {

        final String body = mapper.writeValueAsString(mapper.convertValue(TEST_OWNER, OwnerResponseDTO.class));
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        final OwnerResponseDTO ownerResponseDTO = customersServiceClient.createOwner(TEST_OWNER).block();

        assertEquals(ownerResponseDTO.getOwnerId(),TEST_OWNER.getOwnerId());
        assertEquals(ownerResponseDTO.getFirstName(),TEST_OWNER.getFirstName());
        assertEquals(ownerResponseDTO.getLastName(),TEST_OWNER.getLastName());
        assertEquals(ownerResponseDTO.getAddress(),TEST_OWNER.getAddress());
        assertEquals(ownerResponseDTO.getCity(),TEST_OWNER.getCity());
        assertEquals(ownerResponseDTO.getProvince(),TEST_OWNER.getProvince());
        assertEquals(ownerResponseDTO.getTelephone(),TEST_OWNER.getTelephone());
        //assertEquals(ownerResponseDTO.getImageId(),TEST_OWNER.getImageId());
    }


    @Test
    void getOwnerByOwnerId() throws JsonProcessingException{
        final String body = mapper.writeValueAsString(mapper.convertValue(TEST_OWNER, OwnerResponseDTO.class));
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        final OwnerResponseDTO ownerResponseDTO = customersServiceClient.getOwner("ownerId-123").block();

        assertEquals(ownerResponseDTO.getOwnerId(),TEST_OWNER.getOwnerId());

    }

    @Test
    void getAllOwners() throws JsonProcessingException {
        Flux<OwnerResponseDTO> owners = Flux.just(TEST_OWNER_RESPONSE);

        final String body = mapper.writeValueAsString(owners.collectList().block());

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        final OwnerResponseDTO firstOwnerFromFlux = customersServiceClient.getAllOwners().blockFirst();

        assertEquals(firstOwnerFromFlux.getOwnerId(), TEST_OWNER.getOwnerId());
    }

    @Test
    void getOwnersByPagination() throws JsonProcessingException {
       OwnerResponseDTO TEST_OWNER1 = OwnerResponseDTO.builder()
                .ownerId("ownerId-1")
               .firstName("Test")
               .lastName("Test")
               .address("Test")
               .city("Test")
               .province("Test")
               .telephone("Test")
                //.imageId(1)
                .build();
        OwnerResponseDTO TEST_OWNER2 = OwnerResponseDTO.builder()
                .ownerId("ownerId-2")
                .firstName("Test")
                .lastName("Test")
                .address("Test")
                .city("Test")
                .province("Test")
                .telephone("Test")
                //.imageId(1)
                .build();
        OwnerResponseDTO TEST_OWNER3 = OwnerResponseDTO.builder()
                .ownerId("ownerId-3")
                .firstName("Test")
                .lastName("Test")
                .address("Test")
                .city("Test")
                .province("Test")
                .telephone("Test")
                //.imageId(1)
                .build();

        Flux<OwnerResponseDTO> owners = Flux.just(TEST_OWNER1,TEST_OWNER2);

        final String body = mapper.writeValueAsString(owners.collectList().block());

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        Optional<Integer> page = Optional.of(0);
        Optional<Integer> size =  Optional.of(2);

        final Flux<OwnerResponseDTO> ownersFlux = customersServiceClient.getOwnersByPagination(page,size,null,null,null,null,null);

        Long fluxSize = ownersFlux.count().block();
        Long predictedSize = (long) size.get();


        assertEquals(fluxSize, predictedSize);
    }

    @Test
    void getAllOwnersByPaginationWithFiltersApplied() throws JsonProcessingException {
        OwnerResponseDTO TEST_OWNER1 = OwnerResponseDTO.builder()
                .ownerId("ownerId-1")
                .firstName("FN1")
                .lastName("LN1")
                .address("Test")
                .city("C1")
                .province("Test")
                .telephone("T1")
                //.imageId(1)
                .build();

        Flux<OwnerResponseDTO> owners = Flux.just(TEST_OWNER1);

        final String body = mapper.writeValueAsString(owners.collectList().block());

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        Optional<Integer> page = Optional.of(0);
        Optional<Integer> size =  Optional.of(1);
        String ownerId = "ownerId-1";
        String firstName = "FN1";
        String lastName = "LN1";
        String city = "C1";
        String phoneNumber = "T1";
        final OwnerResponseDTO owner = customersServiceClient.getOwnersByPagination(page,size,ownerId,firstName,lastName,phoneNumber,city).blockFirst();

        assertEquals(ownerId, owner.getOwnerId());
        assertEquals(city, owner.getCity());
    }

    @Test
    void getTotalNumberOfOwners() throws Exception {
        // Simulate the expected total count
        long expectedCount = 0;

        // Prepare the response with the expected count as a plain long value
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(String.valueOf(expectedCount)));

        final Mono<Long> response = customersServiceClient.getTotalNumberOfOwners();

        assertEquals(expectedCount,response.block());
    }

    @Test
    void getTotalNumberOfOwnersWithFilters() throws Exception {
        // Simulate the expected total count
        long expectedCount = 0;

        // Prepare the response with the expected count as a plain long value
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(String.valueOf(expectedCount)));

        final Mono<Long> response = customersServiceClient.getTotalNumberOfOwnersWithFilters(null,null,null,null,null);

        assertEquals(expectedCount,response.block());
    }

    @Test
    void getTotalNumberOfOwnersWithFilters_UnknownValue_ShouldReturnZeroOwners() throws Exception {
        // Simulate the expected total count
        long expectedCount = 0;

        // Prepare the response with the expected count as a plain long value
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(String.valueOf(expectedCount)));

        String ownerId = "unknown";
        String firstName = "unknown";
        String lastName = "unknown";
        String city = "unknown";
        String phoneNumber = "unknown";

        final Mono<Long> response = customersServiceClient.getTotalNumberOfOwnersWithFilters(ownerId,firstName,lastName,phoneNumber,city);

        assertEquals(expectedCount,response.block());
    }


    @Test
    void testPatchPet() throws Exception {
        PetRequestDTO petRequestDTO = new PetRequestDTO(); // Create a request DTO
        petRequestDTO.setPetId("petId-123");
        petRequestDTO.setIsActive("true"); // Set the isActive status

        PetResponseDTO updatedPetResponse = new PetResponseDTO(); // Create an expected response DTO
        updatedPetResponse.setPetId("petId-123");
        updatedPetResponse.setIsActive("true"); // Set the isActive status in the expected response

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(updatedPetResponse))); // Use the expected response DTO

        Mono<PetResponseDTO> responseMono = customersServiceClient.patchPet(petRequestDTO, "petId-123");

        PetResponseDTO responseDTO = responseMono.block(); // Blocking for simplicity

        // Verify the response
        assertEquals(updatedPetResponse.getPetId(), responseDTO.getPetId());
        assertEquals(updatedPetResponse.getIsActive(), responseDTO.getIsActive()); // Check the isActive status
    }

    @Test
    void testDeletePetByPetId() throws Exception {
        // Create a pet id that will be used in the test
        String petId = "petId-123";

        // Set up the mock server to return a 204 (No Content) status code when the deletePetById endpoint is hit
        server.enqueue(new MockResponse()
                .setResponseCode(204));

        // Call the deletePetById method
        Mono<PetResponseDTO> responseMono = customersServiceClient.deletePetByPetId(petId);

        // Block the response for simplicity
        responseMono.block();

        // Verify that the deletePetById endpoint was hit with the correct pet id
        RecordedRequest request = server.takeRequest();
        assertEquals("/pet/" + petId, request.getPath());
        assertEquals("DELETE", request.getMethod());
    }





    @Test
    void testUpdateOwner() throws Exception {
        // Mock the external service's response when updating the owner
        OwnerRequestDTO requestDTO = new OwnerRequestDTO();
        requestDTO.setFirstName("UpdatedFirstName");
        requestDTO.setLastName("UpdatedLastName");

        OwnerResponseDTO updatedOwnerResponse = new OwnerResponseDTO();
        updatedOwnerResponse.setOwnerId("ownerId-123");
        updatedOwnerResponse.setFirstName("UpdatedFirstName");
        updatedOwnerResponse.setLastName("UpdatedLastName");

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(updatedOwnerResponse)));

        Mono<OwnerResponseDTO> responseMono = customersServiceClient.updateOwner("ownerId-123", Mono.just(requestDTO));

        OwnerResponseDTO responseDTO = responseMono.block(); // Blocking for simplicity

        assertEquals(updatedOwnerResponse.getOwnerId(), responseDTO.getOwnerId());
        assertEquals(updatedOwnerResponse.getFirstName(), responseDTO.getFirstName());
        assertEquals(updatedOwnerResponse.getLastName(), responseDTO.getLastName());
    }


    @Test
    void getAllPetTypes() throws JsonProcessingException {
        Flux<PetTypeResponseDTO> petTypes = Flux.just(TEST_PETTYPE_RESPONSE);

        final String body = mapper.writeValueAsString(petTypes.collectList().block());

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        final PetTypeResponseDTO firstPetTypeFromFlux = customersServiceClient.getAllPetTypes().blockFirst();

        assertEquals(firstPetTypeFromFlux.getName(), TEST_PETTYPE.getName());
    }


    @Test
    void testUpdatePet() throws Exception {
        // Given
        String petId = "123";
        PetRequestDTO petRequestDTO = PetRequestDTO.builder()
                .ownerId("owner1")
                .name("Buddy")
                .petTypeId("dog")
                .build();
        PetResponseDTO petResponseDTO = new PetResponseDTO();
        petResponseDTO.setPetId(petId);
        petResponseDTO.setName("Buddy");

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(petResponseDTO)));

        // When
        Mono<PetResponseDTO> result = customersServiceClient.updatePet(Mono.just(petRequestDTO), petId);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getPetId().equals(petId) && response.getName().equals("Buddy"))
                .verifyComplete();

        // Verify the request
        RecordedRequest request = server.takeRequest();
        assertEquals("/pet/" + petId, request.getPath());
        assertEquals("PUT", request.getMethod());
    }
    /*@Test
    void getOwnerPhoto() throws JsonProcessingException {

        final String body = mapper.writeValueAsString(mapper.convertValue(TEST_PHOTO, PhotoDetails.class));
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        final PhotoDetails testPhoto = customersServiceClient.getOwnerPhoto(2).block();

        assertEquals(TEST_PHOTO.getId(), testPhoto.getId());
        assertEquals(TEST_PHOTO.getName(), testPhoto.getName());
        assertEquals(TEST_PHOTO.getType(), testPhoto.getType());
//        assertEquals(TEST_PHOTO.getPhoto(), testPhoto.getPhoto());

    }*/
//    @Test
//    void createPetPhoto() throws JsonProcessingException {
//
//        customersServiceClient.setPetPhoto("ownerId-1", TEST_PHOTO,"1");
//        final String body = mapper.writeValueAsString(mapper.convertValue(TEST_PHOTO, PhotoDetails.class));
//        prepareResponse(response -> response
//                .setHeader("Content-Type", "application/json")
//                .setBody(body));
//
//        final PhotoDetails testPhoto = customersServiceClient.getPetPhoto("ownerId-2", "2").block();
//
//        assertEquals(TEST_PHOTO.getId(), testPhoto.getId());
//        assertEquals(TEST_PHOTO.getName(), testPhoto.getName());
//        assertEquals(TEST_PHOTO.getType(), testPhoto.getType());
////        assertEquals(TEST_PHOTO.getPhoto(), testPhoto.getPhoto());
//
//    }
//    @Test
//    void getPetPhoto() throws JsonProcessingException {
//
//        final String body = mapper.writeValueAsString(mapper.convertValue(TEST_PHOTO, PhotoDetails.class));
//        prepareResponse(response -> response
//                .setHeader("Content-Type", "application/json")
//                .setBody(body));
//
//        final PhotoDetails testPhoto = customersServiceClient.getPetPhoto("ownerId-3","1").block();
//
//        assertEquals(TEST_PHOTO.getId(), testPhoto.getId());
//        assertEquals(TEST_PHOTO.getName(), testPhoto.getName());
//        assertEquals(TEST_PHOTO.getType(), testPhoto.getType());
//    }

//    @Test
//    void deleteOwnerPhoto() throws JsonProcessingException {
//
//        final String body = mapper.writeValueAsString(mapper.convertValue(TEST_PHOTO, PhotoDetails.class));
//        prepareResponse(response -> response
//                .setHeader("Content-Type", "application/json")
//                .setBody(body));
//
//        final Mono<Void> empty = customersServiceClient.deleteOwnerPhoto(TEST_PHOTO.getId());
//
//        assertEquals(empty.block(), null);
//    }

//    @Test
//    void deletePetPhoto() throws JsonProcessingException {
//
//        final String body = mapper.writeValueAsString(mapper.convertValue(TEST_PHOTO, PhotoDetails.class));
//        prepareResponse(response -> response
//                .setHeader("Content-Type", "application/json")
//                .setBody(body));
//
//        final Mono<Void> empty = customersServiceClient.deletePetPhoto(1,TEST_PHOTO.getId());
//
//        assertEquals(empty.block(), null);
//    }

    private void prepareResponse(Consumer<MockResponse> consumer) {
        MockResponse response = new MockResponse();
        consumer.accept(response);
        this.server.enqueue(response);
    }


    //TODO apparently everything???




}
