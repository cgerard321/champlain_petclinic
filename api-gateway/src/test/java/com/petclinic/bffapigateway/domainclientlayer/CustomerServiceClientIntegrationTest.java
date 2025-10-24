package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.*;
import com.petclinic.bffapigateway.dtos.Vets.PhotoDetails;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
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
import java.util.List;
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
    private final String OWNER_ID = "ownerId-123";
    private final String PET_ID = "petId-123";
    private final String PET_TYPE_ID = "petTypeId-123";

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
    void whenAddPetType_ShouldReturnCreatedPetType() throws Exception {
        // Arrange
        final PetTypeRequestDTO request = PetTypeRequestDTO.builder()
                .name("Hamster")
                .petTypeDescription("Small and friendly rodent")
                .build();

        final PetTypeResponseDTO expectedResponse = PetTypeResponseDTO.builder()
                .petTypeId("petTypeId-xyz")
                .name("Hamster")
                .petTypeDescription("Small and friendly rodent")
                .build();

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(expectedResponse)));

        Mono<PetTypeResponseDTO> result =
                customersServiceClient.addPetType(Mono.just(request));

        StepVerifier.create(result)
                .expectNextMatches(r ->
                        r.getPetTypeId().equals(expectedResponse.getPetTypeId()) &&
                                r.getName().equals(expectedResponse.getName()) &&
                                r.getPetTypeDescription().equals(expectedResponse.getPetTypeDescription()))
                .verifyComplete();

        RecordedRequest recorded = server.takeRequest();
        assertEquals("/owners/petTypes", recorded.getPath());
        assertEquals("POST", recorded.getMethod());
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

        Mono<PetResponseDTO> result = customersServiceClient.updatePet(Mono.just(petRequestDTO), petId);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getPetId().equals(petId) && response.getName().equals("Buddy"))
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("/pet/" + petId, request.getPath());
        assertEquals("PUT", request.getMethod());
    }
    @Test
    void whenAddPet_thenReturnCreatedPet() throws Exception {
        PetRequestDTO requestDTO = PetRequestDTO.builder().ownerId(OWNER_ID).name("New Pet").petTypeId("1").build();
        PetResponseDTO responseDTO = PetResponseDTO.builder().petId("new-pet-id").ownerId(OWNER_ID).name("New Pet").petTypeId("1").build();

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(responseDTO)));
        Mono<PetResponseDTO> result = customersServiceClient.addPet(Mono.just(requestDTO));

        StepVerifier.create(result)
                .expectNextMatches(r -> r.getName().equals("New Pet"))
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("/pet", request.getPath());
        assertEquals("POST", request.getMethod());
    }

    @Test
    void whenCreatePetForOwner_thenReturnCreatedPet() throws Exception {
        PetRequestDTO requestDTO = PetRequestDTO.builder().ownerId(OWNER_ID).name("New Owner Pet").petTypeId("1").build();
        PetResponseDTO responseDTO = PetResponseDTO.builder().petId("new-owner-pet-id").ownerId(OWNER_ID).name("New Owner Pet").petTypeId("1").build();

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(responseDTO)));

        Mono<PetResponseDTO> result = customersServiceClient.createPetForOwner(OWNER_ID, requestDTO);

        StepVerifier.create(result)
                .expectNextMatches(r -> r.getName().equals("New Owner Pet") && r.getOwnerId().equals(OWNER_ID))
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("/pet/owners/" + OWNER_ID + "/pets", request.getPath());
        assertEquals("POST", request.getMethod());
    }

    @Test
    void whenGetPetByOwnerIdAndPetId_thenReturnPet() throws Exception {
        final String body = mapper.writeValueAsString(TEST_PET);

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        Mono<PetResponseDTO> result = customersServiceClient.getPet(OWNER_ID, PET_ID);

        StepVerifier.create(result)
                .expectNextMatches(r -> r.getPetId().equals(PET_ID) && r.getOwnerId().equals(OWNER_ID))
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("/owners/" + OWNER_ID + "/pets/" + PET_ID, request.getPath());
    }
//Some of these tests, including this one don't use proper endpoints
    // /pet/owner/{ownerId}/pets should never be an endpoint. Due to circonstances outside the customer team's control
    //The endpoints were brought back to their original names, although they were previously modified.
    //Since it would require another new ticket to change them back, they will remain as is for these tests and will have to be updated in another ticket yet again
    @Test
    void whenGetPetsByOwnerId_thenReturnPetsFlux() throws Exception {
        List<PetResponseDTO> list = List.of(TEST_PET, TEST_PET);
        final String body = mapper.writeValueAsString(list);

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        Flux<PetResponseDTO> result = customersServiceClient.getPetsByOwnerId(OWNER_ID);

        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("/pet/owner/" + OWNER_ID + "/pets", request.getPath()); //worse naming for an endpoint ever
    }

    @Test
    void whenGetAllPets_thenReturnAllPetsFlux() throws Exception {
        List<PetResponseDTO> list = List.of(TEST_PET, TEST_PET);
        final String body = mapper.writeValueAsString(list);

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        Flux<PetResponseDTO> result = customersServiceClient.getAllPets();

        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("/pet", request.getPath());
    }

    @Test
    void whenUpdatePetType_thenReturnUpdatedPetType() throws Exception {
        PetTypeRequestDTO requestDTO = PetTypeRequestDTO.builder().name("Updated Dog").petTypeDescription("Updated Mammal").build();
        PetTypeResponseDTO responseDTO = PetTypeResponseDTO.builder().petTypeId(PET_TYPE_ID).name("Updated Dog").petTypeDescription("Updated Mammal").build();

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(responseDTO)));

        Mono<PetTypeResponseDTO> result = customersServiceClient.updatePetType(PET_TYPE_ID, Mono.just(requestDTO));

        StepVerifier.create(result)
                .expectNextMatches(r -> r.getName().equals("Updated Dog"))
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("/owners/petTypes/" + PET_TYPE_ID, request.getPath());
        assertEquals("PUT", request.getMethod());
    }

    @Test
    void whenDeletePetTypeV2_thenReturnNoContent() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(204));

        Mono<Void> result = customersServiceClient.deletePetTypeV2(PET_TYPE_ID);

        StepVerifier.create(result).verifyComplete();

        RecordedRequest request = server.takeRequest();

        assertEquals("/owners/petTypes/" + PET_TYPE_ID, request.getPath());
        assertEquals("DELETE", request.getMethod());
    }

    @Test
    void whenGetPetTypesByPagination_thenReturnPetTypesFlux() throws Exception {
        List<PetTypeResponseDTO> list = List.of(TEST_PETTYPE_RESPONSE);
        final String body = mapper.writeValueAsString(list);

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        Optional<Integer> page = Optional.of(0);
        Optional<Integer> size =  Optional.of(10);

        Flux<PetTypeResponseDTO> result = customersServiceClient.getPetTypesByPagination(page, size, PET_TYPE_ID, "Dog", null);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
         assertEquals("/owners/petTypes/pet-types-pagination?page=0&size=10&petTypeId=petTypeId-123&name=Dog", request.getPath());
    }

    @Test
    void whenGetTotalNumberOfPetTypes() throws Exception {
        long expectedCount = 5;

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(String.valueOf(expectedCount)));

        Mono<Long> result = customersServiceClient.getTotalNumberOfPetTypes();

        StepVerifier.create(result)
                .expectNext(expectedCount)
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("/owners/petTypes/pet-types-count", request.getPath());
    }

    @Test
    void whenGetTotalNumberOfPetTypesWithFilters_thenReturnCount() throws Exception {
        long expectedCount = 1;

        prepareResponse(response -> response
                .setHeader("Content-Type", "application/json")
                .setBody(String.valueOf(expectedCount)));

        Mono<Long> result = customersServiceClient.getTotalNumberOfPetTypesWithFilters(PET_TYPE_ID, "Dog", null);

        StepVerifier.create(result)
                .expectNext(expectedCount)
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("/owners/petTypes/pet-types-filtered-count?petTypeId=petTypeId-123&name=Dog", request.getPath());
    }

    private void prepareResponse(Consumer<MockResponse> consumer) {
        MockResponse response = new MockResponse();
        consumer.accept(response);
        this.server.enqueue(response);
    }

    @Test
    void whenGetOwnerWithPhoto_thenReturnOwnerWithPhotoData() throws Exception {
        String mockOwnerJson = """
            {
                "ownerId": "ownerId-123",
                "firstName": "aaa",
                "lastName": "bbb",
                "photo": {
                    "fileId": "photo-123",
                    "fileName": "profile.jpg",
                    "fileType": "image/jpeg",
                    "fileData": "bW9ja1Bob3RvRGF0YQ=="
                }
            }
            """;

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(mockOwnerJson));

        Mono<OwnerResponseDTO> result = customersServiceClient.getOwner(OWNER_ID, true);

        StepVerifier.create(result)
                .expectNextMatches(owner -> 
                    owner.getOwnerId().equals("ownerId-123") &&
                    owner.getFirstName().equals("aaa") &&
                    owner.getLastName().equals("bbb") &&
                    owner.getPhoto() != null &&
                    owner.getPhoto().getFileType().equals("image/jpeg"))
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("/owners/" + OWNER_ID + "?includePhoto=true", request.getPath());
        assertEquals("GET", request.getMethod());
    }

    @Test
    void whenGetOwnerWithoutPhoto_thenReturnOwnerWithoutPhotoData() throws Exception {
        String mockOwnerJson = """
            {
                "ownerId": "ownerId-123",
                "firstName": "aaa",
                "lastName": "bbb"
            }
            """;

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(mockOwnerJson));

        Mono<OwnerResponseDTO> result = customersServiceClient.getOwner(OWNER_ID, false);

        StepVerifier.create(result)
                .expectNextMatches(owner -> 
                    owner.getOwnerId().equals("ownerId-123") &&
                    owner.getFirstName().equals("aaa") &&
                    owner.getLastName().equals("bbb") &&
                    owner.getPhoto() == null)
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("/owners/" + OWNER_ID + "?includePhoto=false", request.getPath());
        assertEquals("GET", request.getMethod());
    }

    @Test
    void whenAddOwner_thenReturnCreatedOwner() throws Exception {
        OwnerRequestDTO requestDTO = OwnerRequestDTO.builder()
                .firstName("New")
                .lastName("Owner")
                .address("123 Street")
                .city("City")
                .province("Province")
                .telephone("1234567890")
                .build();

        OwnerResponseDTO responseDTO = OwnerResponseDTO.builder()
                .ownerId("new-owner-id")
                .firstName("New")
                .lastName("Owner")
                .build();

        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(responseDTO)));

        Mono<OwnerResponseDTO> result = customersServiceClient.addOwner(Mono.just(requestDTO));

        StepVerifier.create(result)
                .expectNextMatches(r -> r.getOwnerId().equals("new-owner-id"))
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("/owners", request.getPath());
        assertEquals("POST", request.getMethod());
    }

    @Test
    void whenDeleteOwner_thenReturnDeletedOwner() throws Exception {
        OwnerResponseDTO responseDTO = OwnerResponseDTO.builder()
                .ownerId(OWNER_ID)
                .firstName("Deleted")
                .lastName("Owner")
                .build();

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(responseDTO)));

        Mono<OwnerResponseDTO> result = customersServiceClient.deleteOwner(OWNER_ID);

        StepVerifier.create(result)
                .expectNextMatches(r -> r.getOwnerId().equals(OWNER_ID))
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("/owners/" + OWNER_ID, request.getPath());
        assertEquals("DELETE", request.getMethod());
    }

    @Test
    void whenGetPetByPetId_thenReturnPet() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(TEST_PET)));

        Mono<PetResponseDTO> result = customersServiceClient.getPetByPetId(PET_ID, false);

        StepVerifier.create(result)
                .expectNextMatches(r -> r.getPetId().equals(PET_ID))
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("/pet/" + PET_ID + "?includePhoto=false", request.getPath());
        assertEquals("GET", request.getMethod());
    }

    @Test
    void whenDeletePetType_thenReturnDeletedPetType() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(TEST_PETTYPE_RESPONSE)));

        Mono<PetTypeResponseDTO> result = customersServiceClient.deletePetType(PET_TYPE_ID);

        StepVerifier.create(result)
                .expectNextMatches(r -> r.getPetTypeId().equals(PET_TYPE_ID))
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("/owners/petTypes/" + PET_TYPE_ID, request.getPath());
        assertEquals("DELETE", request.getMethod());
    }

    @Test
    void whenGetPetTypeByPetTypeId_thenReturnPetType() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(TEST_PETTYPE_RESPONSE)));

        Mono<PetTypeResponseDTO> result = customersServiceClient.getPetTypeByPetTypeId(PET_TYPE_ID);

        StepVerifier.create(result)
                .expectNextMatches(r -> r.getPetTypeId().equals(PET_TYPE_ID))
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("/owners/petTypes/" + PET_TYPE_ID, request.getPath());
        assertEquals("GET", request.getMethod());
    }

    @Test
    void whenGetPetTypes_thenReturnPetTypesList() throws Exception {
        PetType petType1 = new PetType();
        petType1.setId(1);
        petType1.setName("Dog");

        PetType petType2 = new PetType();
        petType2.setId(2);
        petType2.setName("Cat");

        List<PetType> petTypes = List.of(petType1, petType2);

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(petTypes)));

        Flux<PetType> result = customersServiceClient.getPetTypes();

        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("/owners/petTypes", request.getPath());
        assertEquals("GET", request.getMethod());
    }

    @Test
    void whenCreatePet_thenReturnCreatedPet() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(TEST_PET)));

        Mono<PetResponseDTO> result = customersServiceClient.createPet(TEST_PET, OWNER_ID);

        StepVerifier.create(result)
                .expectNextMatches(r -> r.getPetId().equals(PET_ID))
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("/pet", request.getPath());
        assertEquals("POST", request.getMethod());
    }

    @Test
    void whenCreateOwners_thenReturnOwnersList() throws Exception {
        List<OwnerResponseDTO> owners = List.of(TEST_OWNER_RESPONSE);

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(mapper.writeValueAsString(owners)));

        Flux<OwnerResponseDTO> result = customersServiceClient.createOwners();

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertEquals("/", request.getPath());
        assertEquals("POST", request.getMethod());
    }

}
