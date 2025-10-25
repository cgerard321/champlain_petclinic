package com.petclinic.bffapigateway.presentationlayer.v1.mockservers;

import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.Parameter.param;


//Due to other groups, some of the changes made to endpoints were removed.
// ideally, the endpoints should consistently be /owners, /petTypes and /pets
//This was fixed in Sprint 1 and changed back by someone in Sprint 2.
//Seeing as it is a fully fledged ticket, the current config will remain as is
// Endpoints should be changed consistently across tests, front-end and api-gateway controllers and clients.
//For future reference, simply look at all classes that have singular endpoints and correct them
public class MockServerConfigCustomersService {

    private static final Integer CUSTOMERS_SERVICE_SERVER_PORT = 7003;

    private final ClientAndServer clientAndServer;

    private final MockServerClient mockServerClient_CustomersService = new MockServerClient("localhost", CUSTOMERS_SERVICE_SERVER_PORT);

    public MockServerConfigCustomersService() {
        this.clientAndServer = ClientAndServer.startClientAndServer(CUSTOMERS_SERVICE_SERVER_PORT);
    }

    public void registerUpdateOwnerEndpoint() {
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("PUT")
                                .withPath("/owners/" + "e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                                .withBody(json("{\"ownerId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\",\"firstName\":\"Betty\",\"lastName\":\"Davis\",\"address\":\"638 Cardinal Ave.\",\"city\":\"Sun Prairie\",\"province\":\"Quebec\",\"telephone\":\"6085551749\"}"))
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json("{\"ownerId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\",\"firstName\":\"Betty\",\"lastName\":\"Davis\",\"address\":\"638 Cardinal Ave.\",\"city\":\"Sun Prairie\",\"province\":\"Quebec\",\"telephone\":\"6085551749\",\"pets\":null}"))
                );
    }

    public void registerAddOwnerEndpoint() {
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/owners")
                                .withBody(json("{\"firstName\":\"Betty\",\"lastName\":\"Davis\",\"address\":\"638 Cardinal Ave.\",\"city\":\"Sun Prairie\",\"province\":\"Quebec\",\"telephone\":\"6085551749\"}"))
                )
                .respond(
                        response()
                                .withStatusCode(201)
                                .withBody(json("{\"ownerId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\",\"firstName\":\"Betty\",\"lastName\":\"Davis\",\"address\":\"638 Cardinal Ave.\",\"city\":\"Sun Prairie\",\"province\":\"Quebec\",\"telephone\":\"6085551749\",\"pets\":null}"))
                );
    }

    public void registerGetAllOwnersEndpoint() {
        String responseBody = "["
                + "{\"ownerId\":\"owner1\",\"firstName\":\"John\",\"lastName\":\"Does\",\"address\":\"123 Main St\",\"city\":\"Springfield\",\"province\":\"Chicago\",\"telephone\":\"1234567890\"},"
                + "{\"ownerId\":\"owner2\",\"firstName\":\"Jane\",\"lastName\":\"Doew\",\"address\":\"456 Maple St\",\"city\":\"Shelbyville\",\"province\":\"Illinois\",\"telephone\":\"0987654321\"},"
                + "{\"ownerId\":\"owner3\",\"firstName\":\"Jim\",\"lastName\":\"Doee\",\"address\":\"789 Oak St\",\"city\":\"Capital City\",\"province\":\"Longueuil\",\"telephone\":\"1122334455\"}"
                + "]";

        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/owners")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json(responseBody))
                );
    }

    public void registerDeleteOwnerEndpoint() {
        OwnerResponseDTO ownerResponse = new OwnerResponseDTO();
        ownerResponse.setOwnerId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a");
        ownerResponse.setFirstName("Betty");
        ownerResponse.setLastName("Davis");
        ownerResponse.setAddress("638 Cardinal Ave.");
        ownerResponse.setCity("Sun Prairie");
        ownerResponse.setProvince("Quebec");
        ownerResponse.setTelephone("6085551749");

        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("DELETE")
                                .withPath("/owners/e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json(ownerResponse))
                                .withHeader("Content-Type", "application/json")
                );
    }

    public void registerGetOwnerByIdEndpoint() {
        String ownerResponseJson = "{"
                + "\"ownerId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\","
                + "\"firstName\":\"Betty\","
                + "\"lastName\":\"Davis\","
                + "\"address\":\"638 Cardinal Ave.\","
                + "\"city\":\"Sun Prairie\","
                + "\"province\":\"Quebec\","
                + "\"telephone\":\"6085551749\","
                + "\"pets\":null"
                + "}";

        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/owners/e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json(ownerResponseJson))
                                .withHeader("Content-Type", "application/json")
                );
    }

    public void registerUpdatePetEndpoint(String petId, PetResponseDTO petResponse) {
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("PUT")
                                .withPath("/pet/" + petId)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json(petResponse))
                );
    }


    public void registerDeletePetEndpoint(String petId) {
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("DELETE")
                                .withPath("/pet/" + petId)
                )
                .respond(
                        response()
                                .withStatusCode(204)
                );
    }

    public void registerGetPetTypesEndpoint() {
        String petType1 = "{\"petTypeId\":\"pt-1\",\"name\":\"Dog\",\"petTypeDescription\":\"Loyal Companion\"}";
        String petType2 = "{\"petTypeId\":\"pt-2\",\"name\":\"Cat\",\"petTypeDescription\":\"Independent Hunter\"}";
        String responseBody = "[" + petType1 + "," + petType2 + "]";

        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/owners/petTypes")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json(responseBody))
                                .withHeader("Content-Type", "application/json")
                );
    }


    public void registerGetEmptyPetTypesEndpoint() {
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/owners/petTypes")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json("[]"))
                                .withHeader("Content-Type", "application/json")
                );
    }

    public void registerGetPetTypesCountEndpoint() {
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/owners/petTypes/pet-types-filtered-count")
                                .withQueryStringParameter(param("name", "Dog"))
                                .withQueryStringParameter(param("petTypeId", "pt-1"))
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody("2")
                                .withHeader("Content-Type", "application/json")
                );
    }

    //This will serve for integration tests using negative paths
    public void registerGetPetTypesCountEndpoint_500() {
        mockServerClient_CustomersService.reset();
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/owners/petTypes/pet-types-filtered-count")
                )
                .respond(
                        response()
                                .withStatusCode(500)
                );
    }
    //This will serve for integration tests using negative paths
    public void registerGetPetTypesEndpoint_500() {
        mockServerClient_CustomersService.reset();
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/owners/petTypes")
                )
                .respond(
                        response()
                                .withStatusCode(500)
                );
    }

    public void registerGetPetTypeByIdEndpoint(String petTypeId) {
        String responseBody = String.format("{\"petTypeId\":\"%s\",\"name\":\"Dog\",\"petTypeDescription\":\"Loyal Companion\"}", petTypeId);

        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/owners/petTypes/" + petTypeId)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json(responseBody))
                                .withHeader("Content-Type", "application/json")
                );
    }
    public void registerGetPetByIdEndpoint(String petId, PetResponseDTO petResponse) {
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/pets/" + petId)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json(petResponse))
                );
    }

    public void registerUpdatePetEndpoint_404(String petId) {
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("PUT")
                                .withPath("/pets/" + petId)
                )
                .respond(
                        response()
                                .withStatusCode(404)
                );
    }



    public void registerPetEndpoint_500(String petId) {
        mockServerClient_CustomersService.reset();
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/pets/" + petId)
                )
                .respond(
                        response()
                                .withStatusCode(500)
                );
    }

    public void registerGetPetForOwnerEndpoint(String ownerId, String petId, PetResponseDTO petResponseDTO) {
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/owners/" + ownerId + "/pets/" + petId)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json(petResponseDTO))
                                .withHeader("Content-Type", "application/json")
                );
    }

    public void registerGetOwnerPhotoEndpointNotFound(String ownerId) {
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/owners/" + ownerId + "/photos")
                )
                .respond(
                        response()
                                .withStatusCode(404)
                );
    }

    public void registerGetOwnerPhotoEndpoint(String ownerId, byte[] photoBytes) {
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/owners/" + ownerId + "/photos")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(photoBytes)
                                .withHeader("Content-Type", "image/png")
                );
    }

    public void registerGetOwnerWithPhotoEndpoint(String ownerId, String ownerJson) {
        mockServerClient_CustomersService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/owners/" + ownerId)
                                .withQueryStringParameter("includePhoto", "true")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(ownerJson)
                                .withHeader("Content-Type", "application/json")
                );
    }

    public void clearExpectationsForOwner(String ownerId) {
        //clear all expectations for this specific owner path
        mockServerClient_CustomersService
                .clear(
                        request()
                                .withMethod("GET")
                                .withPath("/owners/" + ownerId)
                );
    }

    public void stopMockServer() {
        if(clientAndServer != null)
            this.clientAndServer.stop();
    }
}
