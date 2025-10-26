package com.petclinic.bffapigateway.presentationlayer.v2.mockservers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.bffapigateway.dtos.Vets.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Set;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

public class MockServerConfigVetService {

    private static final Integer VET_SERVICE_SERVER_PORT = 7002;

    private final ClientAndServer clientAndServer;

    private final MockServerClient mockServerClient_VetService = new MockServerClient("localhost", VET_SERVICE_SERVER_PORT);

    private final ObjectMapper mapper = new ObjectMapper();

    public MockServerConfigVetService() {
        this.clientAndServer = ClientAndServer.startClientAndServer(VET_SERVICE_SERVER_PORT);

    }


    public void registerGetVetsEndpoint() {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/vets")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json("["
                                        + "{"
                                        + "\"vetId\":\"2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb\","
                                        + "\"vetBillId\":\"bill001\","
                                        + "\"firstName\":\"John\","
                                        + "\"lastName\":\"Doe\","
                                        + "\"email\":\"john.doe@example.com\","
                                        + "\"phoneNumber\":\"1234567890\","
                                        + "\"resume\":\"Specialist in dermatology\","
                                        + "\"workday\":["
                                        + "\"Monday\", \"Wednesday\""
                                        + "],"
                                        + "\"workHoursJson\":\"08:00-16:00\","
                                        + "\"active\":true,"
                                        + "\"specialties\":["
                                        + "{"
                                        + "\"specialtyId\":\"dermatology\","
                                        + "\"name\":\"Dermatology\""
                                        + "}"
                                        + "],"
                                        + "\"photoDefault\":false"
                                        + "},"
                                        + "{"
                                        + "\"vetId\":\"3f87b21b-8f44-4bde-b2a6-abc123ef5678\","
                                        + "\"vetBillId\":\"bill002\","
                                        + "\"firstName\":\"Jane\","
                                        + "\"lastName\":\"Smith\","
                                        + "\"email\":\"jane.smith@example.com\","
                                        + "\"phoneNumber\":\"0987654321\","
                                        + "\"resume\":\"Expert in surgery\","
                                        + "\"workday\":["
                                        + "\"Tuesday\", \"Thursday\""
                                        + "],"
                                        + "\"workHoursJson\":\"09:00-17:00\","
                                        + "\"active\":true,"
                                        + "\"specialties\":["
                                        + "{"
                                        + "\"specialtyId\":\"surgery\","
                                        + "\"name\":\"Surgery\""
                                        + "}"
                                        + "],"
                                        + "\"photoDefault\":true"
                                        + "}"
                                        + "]"))
                );
    }

    public void registerGetVetsEndpoint_withNoVets() {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/no_vets")
                )
                .respond(
                        response()
                                .withStatusCode(404)
                );
    }

    public void registerUpdateVetEndpoint() {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("PUT")
                                .withPath("/vets/c02cbf82-625b-11ee-8c99-0242ac120002")
                                .withBody(json("{"
                                        + "\"vetId\":\"c02cbf82-625b-11ee-8c99-0242ac120002\","
                                        + "\"vetBillId\":\"bill001\","
                                        + "\"firstName\":\"John\","
                                        + "\"lastName\":\"Doe\","
                                        + "\"email\":\"john.doe@example.com\","
                                        + "\"phoneNumber\":\"1234567890\","
                                        + "\"resume\":\"Specialist in dermatology\","
                                        + "\"workday\":["
                                        + "\"Wednesday\""
                                        + "],"
                                        + "\"workHoursJson\":\"08:00-16:00\","
                                        + "\"active\":true,"
                                        + "\"specialties\":["
                                        + "{"
                                        + "\"specialtyId\":\"dermatology\","
                                        + "\"name\":\"Dermatology\""
                                        + "}"
                                        + "]"
                                        + "}"))
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json("{"
                                        + "\"vetId\":\"c02cbf82-625b-11ee-8c99-0242ac120002\","
                                        + "\"vetBillId\":\"bill001\","
                                        + "\"firstName\":\"John\","
                                        + "\"lastName\":\"Doe\","
                                        + "\"email\":\"john.doe@example.com\","
                                        + "\"phoneNumber\":\"1234567890\","
                                        + "\"resume\":\"Specialist in dermatology\","
                                        + "\"workday\":["
                                        + "\"Wednesday\""
                                        + "],"
                                        + "\"workHoursJson\":\"08:00-16:00\","
                                        + "\"active\":true,"
                                        + "\"specialties\":["
                                        + "{"
                                        + "\"specialtyId\":\"dermatology\","
                                        + "\"name\":\"Dermatology\""
                                        + "}"
                                        + "]"
                                        + "}"))
                );
    }

    public void registerUpdateVetEndpoint_withInvalidId() {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("PUT")
                                .withPath("/vets/c02cbf82-625b-11ee-8c99-0242ac120002") // The vetId to update
                                .withBody(json("{"
                                        + "\"vetId\":\"invalid-vet-id\","
                                        + "\"vetBillId\":\"bill001\","
                                        + "\"firstName\":\"John\","
                                        + "\"lastName\":\"Doe\","
                                        + "\"email\":\"john.doe@example.com\","
                                        + "\"phoneNumber\":\"1234567890\","
                                        + "\"resume\":\"Specialist in dermatology\","
                                        + "\"workday\":["
                                        + "\"Wednesday\""
                                        + "],"
                                        + "\"workHoursJson\":\"08:00-16:00\","
                                        + "\"active\":true,"
                                        + "\"specialties\":["
                                        + "{"
                                        + "\"specialtyId\":\"dermatology\","
                                        + "\"name\":\"Dermatology\""
                                        + "}"
                                        + "]"
                                        + "}"))
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json("{"
                                        + "\"vetId\":\"invalid-vet-id\","
                                        + "\"vetBillId\":\"bill001\","
                                        + "\"firstName\":\"John\","
                                        + "\"lastName\":\"Doe\","
                                        + "\"email\":\"john.doe@example.com\","
                                        + "\"phoneNumber\":\"1234567890\","
                                        + "\"resume\":\"Specialist in dermatology\","
                                        + "\"workday\":["
                                        + "\"Wednesday\""
                                        + "],"
                                        + "\"workHoursJson\":\"08:00-16:00\","
                                        + "\"active\":true,"
                                        + "\"specialties\":["
                                        + "{"
                                        + "\"specialtyId\":\"dermatology\","
                                        + "\"name\":\"Dermatology\""
                                        + "}"
                                        + "]"
                                        + "}"))
                );
    }

    public void stopMockServer() {
        if (clientAndServer != null)
            this.clientAndServer.stop();
    }


    public void registerGetVetByIdEndpoint() {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/vets/ac9adeb8-625b-11ee-8c99-0242ac120002")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json(new VetResponseDTO(
                                        "ac9adeb8-625b-11ee-8c99-0242ac120002",
                                        "5",
                                        "Henry",
                                        "Stevens",
                                        "stevenshenry@email.com",
                                        "(514)-634-8276 #2389",
                                        "Practicing since 1 years",
                                        Set.of(Workday.Wednesday, Workday.Tuesday, Workday.Thursday, Workday.Monday),
                                        "{\"Thursday\":[\"Hour_8_9\",\"Hour_9_10\",\"Hour_10_11\",\"Hour_11_12\"],"
                                                + "\"Monday\":[\"Hour_8_9\",\"Hour_9_10\",\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\"],"
                                                + "\"Wednesday\":[\"Hour_10_11\",\"Hour_11_12\",\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\"],"
                                                + "\"Tuesday\":[\"Hour_12_13\",\"Hour_13_14\",\"Hour_14_15\",\"Hour_15_16\",\"Hour_16_17\",\"Hour_17_18\",\"Hour_18_19\",\"Hour_19_20\"]}",
                                        false,
                                        Set.of(
                                                new SpecialtyDTO("surgery", "surgery"),
                                                new SpecialtyDTO("radiology", "radiology")
                                        ),
                                        null
                                ))));
    }

    public void registerGetVetByInvalidIdEndpoint() {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/vets/ac9adeb8-625b-11ee-8c99-0242ac12000200000")
                )
                .respond(
                        response()
                                .withStatusCode(404)
                                .withBody("{\"statusCode\":404,\"message\":\"vetId not found: invalid-id\",\"timestamp\":\"" + java.time.Instant.now() + "\"}")
                );
    }


    public void registerDeleteVetEndpoint() {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("DELETE")
                                .withPath("/vets/[a-f0-9\\-]+")
                )
                .respond(
                        response()
                                .withStatusCode(204)
                );
    }

    public void registerGetPhotoByVetIdEndpoint(String vetId, byte[] photoData) {
        // Create PhotoResponseDTO with the photo data
        PhotoResponseDTO photoResponseDTO = PhotoResponseDTO.builder()
                .vetId(vetId)
                .filename("vet_photo.jpg")
                .imgType("image/jpeg")
                .resourceBase64(java.util.Base64.getEncoder().encodeToString(photoData))
                .build();

        String jsonResponse;
        try {
            jsonResponse = mapper.writeValueAsString(photoResponseDTO);
        } catch (JsonProcessingException e) {
            jsonResponse = "{\"error\":\"Failed to serialize photo response\"}";
        }

        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/vets/" + vetId + "/photo")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody(jsonResponse)
                );
    }

    public void registerGetPhotoByVetIdEndpointNotFound(String vetId) {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/vets/" + vetId + "/photo")
                )
                .respond(
                        response()
                                .withStatusCode(404)
                );
    }

    public void registerUpdatePhotoOfVetEndpoint(String vetId, String photoName, byte[] photoData) {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("PUT")
                                .withPath("/vets/" + vetId + "/photo/" + photoName)
                                .withHeader("Content-Type", MediaType.IMAGE_JPEG_VALUE)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader("Content-Type", MediaType.IMAGE_JPEG_VALUE)
                                .withBody(photoData)
                );
    }

    public void registerUpdatePhotoOfVetEndpointNotFound(String vetId, String photoName) {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("PUT")
                                .withPath("/vets/" + vetId + "/photo/" + photoName)
                )
                .respond(
                        response()
                                .withStatusCode(404)
                );
    }


    public void registerGetAlbumsByVetIdEndpoint(String vetId, List<Album> albums) throws JsonProcessingException {
        mockServerClient_VetService.when(
                        request()
                                .withMethod("GET")
                                .withPath("/vets/" + vetId + "/albums"))
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(mapper.writeValueAsString(albums)));
    }

    public void registerGetAlbumsByVetIdEndpointNotFound(String vetId) {
        mockServerClient_VetService.when(
                        request()
                                .withMethod("GET")
                                .withPath("/" + vetId + "/albums"))
                .respond(
                        response()
                                .withStatusCode(404));
    }

    public void registerDeletePhotoByVetIdEndpoint(String vetId) {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("DELETE")
                                .withPath("/vets/" + vetId + "/photo")
                ).respond(
                        response()
                                .withStatusCode(204)
                );
    }

    public void registerDeletePhotoByVetIdEndpointNotFound(String vetId) {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("DELETE")
                                .withPath("/vets/" + vetId + "/photo")
                ).respond(
                        response()
                                .withStatusCode(404)
                                .withHeader("Content-Type", "application/json")
                                .withBody(json("{\"message\":\"Photo not found for vetId: " + vetId + "\"}"))
                );
    }

    public void registerDeleteAlbumPhotoEndpoint(String vetId, Integer albumId) {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("DELETE")
                                .withPath("/vets/" + vetId + "/albums/" + albumId)
                )
                .respond(
                        response()
                                .withStatusCode(204) // 204 No Content indicates successful deletion
                );
    }

    public void registerDeleteAlbumPhotoEndpointNotFound(String vetId, Integer albumId) {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("DELETE")
                                .withPath("/vets/" + vetId + "/albums/" + albumId)
                )
                .respond(
                        response()
                                .withStatusCode(404) // 404 Not Found indicates the album photo does not exist
                                .withHeader("Content-Type", "application/json")
                                .withBody(json("{\"message\":\"Album photo not found: " + albumId + "\"}"))
                );
    }

    public void registerDeleteAlbumPhotoEndpointWithServerError(String vetId, Integer albumId) {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("DELETE")
                                .withPath("/vets/" + vetId + "/albums/" + albumId)
                )
                .respond(
                        response()
                                .withStatusCode(500) // 500 Internal Server Error
                                .withHeader("Content-Type", "application/json")
                                .withBody(json("{\"message\":\"Server error occurred while deleting album photo with ID: " + albumId + "\"}"))
                );
    }

    public void registerGetEducationByVetIdEndpoint(String vetId, List<EducationRequestDTO> educations) throws JsonProcessingException {
        mockServerClient_VetService.when(
                        request()
                                .withMethod("GET")
                                .withPath("/vets/" + vetId + "/educations"))
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(mapper.writeValueAsString(educations)));
    }

    public void registerGetEducationByVetIdEndpointNotFound(String vetId) {
        mockServerClient_VetService.when(
                        request()
                                .withMethod("GET")
                                .withPath("/vets/" + vetId + "/educations"))
                .respond(
                        response()
                                .withStatusCode(404));
    }

    public void registerUpdateEducationByVetIdAndEducationIdEndpoint(String vetId, String educationId, EducationRequestDTO education) {
        mockServerClient_VetService.when(
                        request()
                                .withMethod("PUT")
                                .withPath("/vets/" + vetId + "/educations/" + educationId)
                                .withBody(json(education))
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json(education))
                );
    }

    public void registerAddEducationAsUnauthorizedUser(String vetId) {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/vets/" + vetId + "/educations")
                                .withHeader("Authorization", "Bearer invalid-token")
                                .withBody(json("{"
                                        + "\"vetId\":\"2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb\","
                                        + "\"degree\":\"Doctor of Veterinary Medicine\","
                                        + "\"schoolName\":\"Harvard University\","
                                        + "\"fieldOfStudy\":\"Veterinary Science\","
                                        + "\"startDate\":\"2015-08-01\","
                                        + "\"endDate\":\"2019-05-30\""
                                        + "}"))
                )
                .respond(
                        response()
                                .withStatusCode(403)
                                .withBody("{\"status\":403,\"message\":\"Unauthorized access: User is not admin or vet.\"}")
                );
    }



    public void registerUpdateEducationByVetIdAndEducationIdEndpointNotFound(String vetId, String educationId, EducationRequestDTO education) {
        mockServerClient_VetService.when(
                        request()
                                .withMethod("PUT")
                                .withPath("/vets/" + vetId + "/educations/" + educationId)
                                .withBody(json(education))
                )
                .respond(
                        response()
                                .withStatusCode(404)
                                .withBody("{\"message\":\"Education not found for vetId: " + vetId + " and educationId: " + educationId + "\"}")
                );
    }

    public void registerGetRatingsByVetIdEndpoint(String vetId, String ratingsJson) {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/vets/" + vetId + "/ratings")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody(ratingsJson)
                );
    }

    public void registerGetRatingsByVetIdEndpointNotFound(String vetId) {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/vets/" + vetId + "/ratings")
                )
                .respond(
                        response()
                                .withStatusCode(404)
                                .withBody("{\"message\":\"No ratings found for vetId: " + vetId + "\"}")
                );
    }

    public void registerDeleteEducationEndpoint(String vetId, String educationId) {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("DELETE")
                                .withPath("/vets/" + vetId + "/educations/" + educationId)
                )
                .respond(
                        response()
                                .withStatusCode(204)
                );
    }

    public void registerDeleteEducationByVetIdEndpointNotFound(String vetId, String educationId) {
        String responseBody = "{\"message\":\"Education not found: " + educationId + "\"}";
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("DELETE")
                                .withPath("/vets/" + vetId + "/educations/" + educationId)
                )
                .respond(
                        response()
                                .withStatusCode(404)
                                .withHeader("Content-Type", "application/json")
                                .withBody(responseBody)
                );
    }

    public void registerAddPhotoByVetIdMultipartEndpoint(String vetId, String photoName) {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/vets/" + vetId + "/photos")
                                .withHeader("Content-Type", "application/json")
                )
                .respond(
                        response()
                                .withStatusCode(201)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"vetId\": \"" + vetId + "\",\"filename\": \"" + photoName + "\",\"imgType\": \"image/jpeg\",\"resourceBase64\": \"bW9ja1Bob3RvUmVzb3VyY2U=\"}")
                );
    }

    public void registerAddPhotoByVetIdMultipartEndpointBadRequest(String vetId) {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/vets/" + vetId + "/photos")
                                .withHeader("Content-Type", "application/json")
                )
                .respond(
                        response()
                                .withStatusCode(400)
                                .withBody("{\"message\":\"Bad request - invalid photo data\"}")
                );
    }

    public void registerAddAlbumPhotoOctetEndpoint(String vetId, String photoName) {
        String albumJson = "{"
                + "\"id\": 1,"
                + "\"vetId\": \"" + vetId + "\","
                + "\"filename\": \"" + photoName + "\","
                + "\"imgType\": \"image/jpeg\","
                + "\"data\": \"" + java.util.Base64.getEncoder().encodeToString("mockPhotoData".getBytes()) + "\""
                + "}";

        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/vets/" + vetId + "/albums/photos/" + photoName)
                                .withHeader("Content-Type", "application/octet-stream")
                )
                .respond(
                        response()
                                .withStatusCode(201)
                                .withHeader("Content-Type", "application/json")
                                .withBody(albumJson)
                );
    }

    public void registerAddAlbumPhotoOctetEndpointBadRequest(String vetId) {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/vets/" + vetId + "/albums/photos/.*")
                                .withHeader("Content-Type", "application/octet-stream")
                )
                .respond(
                        response()
                                .withStatusCode(400)
                                .withBody("{\"message\":\"Bad request - invalid photo data\"}")
                );
    }

    public void registerAddAlbumPhotoMultipartEndpoint(String vetId, String photoName) {
        String albumJson = "{"
                + "\"id\": 2,"
                + "\"vetId\": \"" + vetId + "\","
                + "\"filename\": \"" + photoName + "\","
                + "\"imgType\": \"image/jpeg\","
                + "\"data\": \"" + java.util.Base64.getEncoder().encodeToString("mockPhotoData".getBytes()) + "\""
                + "}";

        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/vets/" + vetId + "/albums/photos/" + photoName)
                                .withHeader("Content-Type", "application/octet-stream")
                )
                .respond(
                        response()
                                .withStatusCode(201)
                                .withHeader("Content-Type", "application/json")
                                .withBody(albumJson)
                );
    }

    public void registerAddAlbumPhotoMultipartEndpointBadRequest(String vetId) {
        mockServerClient_VetService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/vets/" + vetId + "/albums/photos/.*")
                                .withHeader("Content-Type", "application/octet-stream")
                )
                .respond(
                        response()
                                .withStatusCode(400)
                                .withBody("{\"message\":\"Bad request - invalid photo data\"}")
                );
    }


}
