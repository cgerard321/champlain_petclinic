package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetType;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static reactor.core.publisher.Mono.just;

public class CustomerServiceClientIntegrationTest {

    private CustomersServiceClient customersServiceClient;

    private MockWebServer server;

    private ObjectMapper mapper;

    final String test = "Test photo";
    final byte[] testBytes = test.getBytes();

<<<<<<< HEAD
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
            .telephone("5553334444")
            //.imageId(1)
            .build();


    private final OwnerResponseDTO TEST_OWNER_RESPONSE = OwnerResponseDTO.builder()
=======
    private final OwnerResponseDTO TEST_OWNER = OwnerResponseDTO.builder()
>>>>>>> 484b3f73 (GetPhotoByVetId all changed files)
            .ownerId("ownerId-123")
            .firstName("John")
            .lastName("Smith")
            .address("456 Elm")
            .city("Montreal")
            .telephone("5553334444")
            //.imageId(1)
            .build();
    PetType type = new PetType();

    private final PetResponseDTO TEST_PET = PetResponseDTO.builder()
            .name("Cat")
            .petId("1")
            .name("Bonkers")
            .birthDate("2015-03-03")
            .type(type)
            .imageId(2)
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
    void testUpdatePet() throws Exception {
        PetResponseDTO petRequestDTO = new PetResponseDTO(); // Create a request DTO
        petRequestDTO.setPetId("petId-123");
        petRequestDTO.setName("UpdatedName");

        PetResponseDTO updatedPetResponse = new PetResponseDTO(); // Create an expected response DTO
        updatedPetResponse.setPetId("petId-123");
        updatedPetResponse.setName("UpdatedName");

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(updatedPetResponse))); // Use the expected response DTO

        Mono<PetResponseDTO> responseMono = customersServiceClient.updatePet(petRequestDTO, "petId-123");

        PetResponseDTO responseDTO = responseMono.block(); // Blocking for simplicity

        // Verify the response
        assertEquals(updatedPetResponse.getPetId(), responseDTO.getPetId());
        assertEquals(updatedPetResponse.getName(), responseDTO.getName());
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
