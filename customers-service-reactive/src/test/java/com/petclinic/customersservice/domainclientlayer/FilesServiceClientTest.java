package com.petclinic.customersservice.domainclientlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.customersservice.customersExceptions.exceptions.BadRequestException;
import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.customersExceptions.exceptions.UnprocessableEntityException;
import com.petclinic.customersservice.util.Rethrower;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.lang.reflect.Field;

class FilesServiceClientTest {

    private static MockWebServer mockBackEnd;
    private FilesServiceClient filesServiceClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Rethrower rethrower = new Rethrower(objectMapper);

    @BeforeAll
    static void setup() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @BeforeEach
    void initialize() throws Exception {
        WebClient.Builder webClientBuilder = WebClient.builder();
        filesServiceClient = new FilesServiceClient(
                webClientBuilder,
                "localhost",
                String.valueOf(mockBackEnd.getPort())
        );
        
        Field rethrowerField = FilesServiceClient.class.getDeclaredField("rethrower");
        rethrowerField.setAccessible(true);
        rethrowerField.set(filesServiceClient, rethrower);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    void getFile_WithValidFileId_ShouldReturnFile() throws Exception {
        FileResponseDTO expectedResponse = FileResponseDTO.builder()
                .fileId("file1")
                .fileName("test.jpg")
                .fileType("image/jpeg")
                .fileData("base64data")
                .build();

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(expectedResponse)));

        StepVerifier.create(filesServiceClient.getFile("file1"))
                .expectNextMatches(response ->
                        response.getFileId().equals("file1") &&
                                response.getFileName().equals("test.jpg") &&
                                response.getFileType().equals("image/jpeg"))
                .verifyComplete();
    }

    @Test
    void getFile_WithNotFoundStatus_ShouldThrowNotFoundException() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"File not found\"}"));

        StepVerifier.create(filesServiceClient.getFile("missing"))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void getFile_WithBadRequestStatus_ShouldThrowBadRequestException() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"Bad request\"}"));

        StepVerifier.create(filesServiceClient.getFile("invalid"))
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    void getFile_WithInternalServerError_ShouldThrowRuntimeException() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"Internal server error\"}"));

        StepVerifier.create(filesServiceClient.getFile("file1"))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void addFile_WithValidRequest_ShouldReturnCreatedFile() throws Exception {
        FileServiceRequestDTO requestDTO = FileServiceRequestDTO.builder()
                .fileName("newfile.jpg")
                .fileType("image/jpeg")
                .fileData("base64data")
                .build();

        FileResponseDTO expectedResponse = FileResponseDTO.builder()
                .fileId("newfile1")
                .fileName("newfile.jpg")
                .fileType("image/jpeg")
                .fileData("base64data")
                .build();

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(201)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(expectedResponse)));

        StepVerifier.create(filesServiceClient.addFile(requestDTO))
                .expectNextMatches(response ->
                        response.getFileId().equals("newfile1") &&
                                response.getFileName().equals("newfile.jpg"))
                .verifyComplete();
    }

    @Test
    void addFile_WithUnprocessableEntityStatus_ShouldThrowUnprocessableEntityException() {
        FileServiceRequestDTO requestDTO = FileServiceRequestDTO.builder()
                .fileName("invalid.jpg")
                .fileType("image/jpeg")
                .fileData("")
                .build();

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(422)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"Unprocessable entity\"}"));

        StepVerifier.create(filesServiceClient.addFile(requestDTO))
                .expectError(UnprocessableEntityException.class)
                .verify();
    }

    @Test
    void addFile_WithBadRequestStatus_ShouldThrowBadRequestException() {
        FileServiceRequestDTO requestDTO = FileServiceRequestDTO.builder()
                .fileName("test.jpg")
                .fileType("image/jpeg")
                .fileData("base64data")
                .build();

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"Bad request\"}"));

        StepVerifier.create(filesServiceClient.addFile(requestDTO))
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    void addFile_WithInternalServerError_ShouldThrowRuntimeException() {
        FileServiceRequestDTO requestDTO = FileServiceRequestDTO.builder()
                .fileName("test.jpg")
                .fileType("image/jpeg")
                .fileData("base64data")
                .build();

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"Internal server error\"}"));

        StepVerifier.create(filesServiceClient.addFile(requestDTO))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void updateFile_WithValidRequest_ShouldReturnUpdatedFile() throws Exception {
        FileServiceRequestDTO requestDTO = FileServiceRequestDTO.builder()
                .fileName("updated.jpg")
                .fileType("image/jpeg")
                .fileData("updatedbase64data")
                .build();

        FileResponseDTO expectedResponse = FileResponseDTO.builder()
                .fileId("file1")
                .fileName("updated.jpg")
                .fileType("image/jpeg")
                .fileData("updatedbase64data")
                .build();

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(expectedResponse)));

        StepVerifier.create(filesServiceClient.updateFile("file1", requestDTO))
                .expectNextMatches(response ->
                        response.getFileId().equals("file1") &&
                                response.getFileName().equals("updated.jpg"))
                .verifyComplete();
    }

    @Test
    void updateFile_WithNotFoundStatus_ShouldThrowNotFoundException() {
        FileServiceRequestDTO requestDTO = FileServiceRequestDTO.builder()
                .fileName("updated.jpg")
                .fileType("image/jpeg")
                .fileData("updatedbase64data")
                .build();

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"File not found\"}"));

        StepVerifier.create(filesServiceClient.updateFile("missing", requestDTO))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void updateFile_WithUnprocessableEntityStatus_ShouldThrowUnprocessableEntityException() {
        FileServiceRequestDTO requestDTO = FileServiceRequestDTO.builder()
                .fileName("updated.jpg")
                .fileType("image/jpeg")
                .fileData("")
                .build();

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(422)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"Unprocessable entity\"}"));

        StepVerifier.create(filesServiceClient.updateFile("file1", requestDTO))
                .expectError(UnprocessableEntityException.class)
                .verify();
    }

    @Test
    void updateFile_WithBadRequestStatus_ShouldThrowBadRequestException() {
        FileServiceRequestDTO requestDTO = FileServiceRequestDTO.builder()
                .fileName("updated.jpg")
                .fileType("image/jpeg")
                .fileData("updatedbase64data")
                .build();

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"Bad request\"}"));

        StepVerifier.create(filesServiceClient.updateFile("file1", requestDTO))
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    void updateFile_WithInternalServerError_ShouldThrowRuntimeException() {
        FileServiceRequestDTO requestDTO = FileServiceRequestDTO.builder()
                .fileName("updated.jpg")
                .fileType("image/jpeg")
                .fileData("updatedbase64data")
                .build();

        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"Internal server error\"}"));

        StepVerifier.create(filesServiceClient.updateFile("file1", requestDTO))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void deleteFile_WithValidFileId_ShouldCompleteSuccessfully() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(204));

        StepVerifier.create(filesServiceClient.deleteFile("file1"))
                .verifyComplete();
    }

    @Test
    void deleteFile_WithNotFoundStatus_ShouldThrowNotFoundException() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"File not found\"}"));

        StepVerifier.create(filesServiceClient.deleteFile("missing"))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void deleteFile_WithBadRequestStatus_ShouldThrowBadRequestException() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"Bad request\"}"));

        StepVerifier.create(filesServiceClient.deleteFile("invalid"))
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    void deleteFile_WithInternalServerError_ShouldThrowRuntimeException() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(500)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"message\":\"Internal server error\"}"));

        StepVerifier.create(filesServiceClient.deleteFile("file1"))
                .expectError(RuntimeException.class)
                .verify();
    }
}
