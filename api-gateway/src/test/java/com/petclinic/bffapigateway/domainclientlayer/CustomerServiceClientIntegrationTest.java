package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetType;
import com.petclinic.bffapigateway.dtos.Vets.PhotoDetails;
import okhttp3.Response;
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

    private final OwnerResponseDTO TEST_OWNER = OwnerResponseDTO.builder()
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
            .id(1)
            .name("Bonkers")
            .birthDate("2015-03-03")
            .type(type)
            .imageId(2)
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
        Flux<OwnerResponseDTO> owners = Flux.just(TEST_OWNER);

        final String body = mapper.writeValueAsString(owners.collectList().block());

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        final OwnerResponseDTO firstOwnerFromFlux = customersServiceClient.getAllOwners().blockFirst();

        assertEquals(firstOwnerFromFlux.getOwnerId(), TEST_OWNER.getOwnerId());
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
    @Test
    void createPetPhoto() throws JsonProcessingException {

        customersServiceClient.setPetPhoto("ownerId-1", TEST_PHOTO,1);
        final String body = mapper.writeValueAsString(mapper.convertValue(TEST_PHOTO, PhotoDetails.class));
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        final PhotoDetails testPhoto = customersServiceClient.getPetPhoto("ownerId-2", 2).block();

        assertEquals(TEST_PHOTO.getId(), testPhoto.getId());
        assertEquals(TEST_PHOTO.getName(), testPhoto.getName());
        assertEquals(TEST_PHOTO.getType(), testPhoto.getType());
//        assertEquals(TEST_PHOTO.getPhoto(), testPhoto.getPhoto());

    }
    @Test
    void getPetPhoto() throws JsonProcessingException {

        final String body = mapper.writeValueAsString(mapper.convertValue(TEST_PHOTO, PhotoDetails.class));
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        final PhotoDetails testPhoto = customersServiceClient.getPetPhoto("ownerId-3",1).block();

        assertEquals(TEST_PHOTO.getId(), testPhoto.getId());
        assertEquals(TEST_PHOTO.getName(), testPhoto.getName());
        assertEquals(TEST_PHOTO.getType(), testPhoto.getType());
    }

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

    @Test
    void deletePetPhoto() throws JsonProcessingException {

        final String body = mapper.writeValueAsString(mapper.convertValue(TEST_PHOTO, PhotoDetails.class));
        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        final Mono<Void> empty = customersServiceClient.deletePetPhoto(1,TEST_PHOTO.getId());

        assertEquals(empty.block(), null);
    }

    private void prepareResponse(Consumer<MockResponse> consumer) {
        MockResponse response = new MockResponse();
        consumer.accept(response);
        this.server.enqueue(response);
    }


    //TODO apparently everything???


}
