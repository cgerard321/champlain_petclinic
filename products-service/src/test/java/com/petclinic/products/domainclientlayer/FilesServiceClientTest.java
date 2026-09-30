package com.petclinic.products.domainclientlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.products.utils.exceptions.BadRequestException;
import com.petclinic.products.utils.exceptions.FailedDependencyException;
import com.petclinic.products.utils.exceptions.FileNotFoundInFilesServiceException;
import com.petclinic.products.utils.exceptions.UnprocessableEntityException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FilesServiceClientTest {

    private static final String FILE_ID = "file-1";
    private static MockWebServer mockBackEnd;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private FilesServiceClient filesServiceClient;

    @BeforeAll
    static void setUpServer() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @BeforeEach
    void setUpClient() {
        filesServiceClient = new FilesServiceClient(
                WebClient.builder(), "localhost", String.valueOf(mockBackEnd.getPort()));
    }

    @AfterAll
    static void tearDownServer() throws IOException {
        mockBackEnd.shutdown();
    }

    @AfterEach
    void clearRecordedRequests() throws InterruptedException {
        while (mockBackEnd.takeRequest(10, TimeUnit.MILLISECONDS) != null) {
            // Drain requests so every test starts with an empty recording queue.
        }
    }

    @Test
    void getFileReturnsFile() throws Exception {
        enqueueJson(200, validResponse());

        StepVerifier.create(filesServiceClient.getFile(FILE_ID))
                .assertNext(response -> {
                    assertEquals(FILE_ID, response.getFileId());
                    assertEquals("product.png", response.getFileName());
                })
                .verifyComplete();

        assertRequest("GET", "/files/" + FILE_ID);
    }

    @Test
    void getFileMapsNotFound() {
        enqueueStatus(404);

        StepVerifier.create(filesServiceClient.getFile(FILE_ID))
                .expectError(FileNotFoundInFilesServiceException.class)
                .verify();
    }

    @Test
    void getFileMapsBadRequest() {
        enqueueStatus(400);

        StepVerifier.create(filesServiceClient.getFile(FILE_ID))
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    void getFileMapsServerError() {
        enqueueStatus(500);

        StepVerifier.create(filesServiceClient.getFile(FILE_ID))
                .expectError(FailedDependencyException.class)
                .verify();
    }

    @Test
    void getFileRejectsResponseWithoutFileId() throws Exception {
        enqueueJson(200, FileResponseDTO.builder().fileName("product.png").build());

        StepVerifier.create(filesServiceClient.getFile(FILE_ID))
                .expectError(FailedDependencyException.class)
                .verify();
    }

    @Test
    void getFileMapsMalformedJson() {
        mockBackEnd.enqueue(jsonResponse(200).setBody("not-json"));

        StepVerifier.create(filesServiceClient.getFile(FILE_ID))
                .expectError(FailedDependencyException.class)
                .verify();
    }

    @Test
    void getFileMapsConnectionFailure() {
        FilesServiceClient unavailableClient =
                new FilesServiceClient(WebClient.builder(), "127.0.0.1", "1");

        StepVerifier.create(unavailableClient.getFile(FILE_ID))
                .expectError(FailedDependencyException.class)
                .verify();
    }

    @Test
    void addFileReturnsCreatedFile() throws Exception {
        enqueueJson(201, validResponse());

        StepVerifier.create(filesServiceClient.addFile(validRequest()))
                .expectNextMatches(response -> FILE_ID.equals(response.getFileId()))
                .verifyComplete();

        RecordedRequest request = assertRequest("POST", "/files/");
        assertNotNull(request.getBody());
    }

    @Test
    void addFileMapsUnprocessableEntity() {
        enqueueStatus(422);

        StepVerifier.create(filesServiceClient.addFile(validRequest()))
                .expectError(UnprocessableEntityException.class)
                .verify();
    }

    @Test
    void addFileMapsBadRequest() {
        enqueueStatus(400);

        StepVerifier.create(filesServiceClient.addFile(validRequest()))
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    void addFileMapsServerError() {
        enqueueStatus(500);

        StepVerifier.create(filesServiceClient.addFile(validRequest()))
                .expectError(FailedDependencyException.class)
                .verify();
    }

    @Test
    void addFileRejectsEmptyResponse() {
        mockBackEnd.enqueue(new MockResponse().setResponseCode(201));

        StepVerifier.create(filesServiceClient.addFile(validRequest()))
                .expectError(FailedDependencyException.class)
                .verify();
    }

    @Test
    void updateFileReturnsUpdatedFile() throws Exception {
        enqueueJson(200, validResponse());

        StepVerifier.create(filesServiceClient.updateFile(FILE_ID, validRequest()))
                .expectNextMatches(response -> FILE_ID.equals(response.getFileId()))
                .verifyComplete();

        assertRequest("PUT", "/files/" + FILE_ID);
    }

    @Test
    void updateFileMapsNotFound() {
        enqueueStatus(404);

        StepVerifier.create(filesServiceClient.updateFile(FILE_ID, validRequest()))
                .expectError(FileNotFoundInFilesServiceException.class)
                .verify();
    }

    @Test
    void updateFileMapsUnprocessableEntity() {
        enqueueStatus(422);

        StepVerifier.create(filesServiceClient.updateFile(FILE_ID, validRequest()))
                .expectError(UnprocessableEntityException.class)
                .verify();
    }

    @Test
    void updateFileMapsBadRequest() {
        enqueueStatus(400);

        StepVerifier.create(filesServiceClient.updateFile(FILE_ID, validRequest()))
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    void updateFileMapsServerError() {
        enqueueStatus(500);

        StepVerifier.create(filesServiceClient.updateFile(FILE_ID, validRequest()))
                .expectError(FailedDependencyException.class)
                .verify();
    }

    @Test
    void updateFileRejectsResponseWithoutFileId() throws Exception {
        enqueueJson(200, FileResponseDTO.builder().fileId(" ").build());

        StepVerifier.create(filesServiceClient.updateFile(FILE_ID, validRequest()))
                .expectError(FailedDependencyException.class)
                .verify();
    }

    @Test
    void deleteFileCompletes() throws Exception {
        mockBackEnd.enqueue(new MockResponse().setResponseCode(204));

        StepVerifier.create(filesServiceClient.deleteFile(FILE_ID)).verifyComplete();

        assertRequest("DELETE", "/files/" + FILE_ID);
    }

    @Test
    void deleteFileTreatsMissingFileAsAlreadyDeleted() {
        enqueueStatus(404);

        StepVerifier.create(filesServiceClient.deleteFile(FILE_ID)).verifyComplete();
    }

    @Test
    void deleteFileMapsBadRequest() {
        enqueueStatus(400);

        StepVerifier.create(filesServiceClient.deleteFile(FILE_ID))
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    void deleteFileMapsServerError() {
        enqueueStatus(500);

        StepVerifier.create(filesServiceClient.deleteFile(FILE_ID))
                .expectError(FailedDependencyException.class)
                .verify();
    }

    private FileRequestDTO validRequest() {
        return FileRequestDTO.builder()
                .fileName("product.png")
                .fileType("image/png")
                .fileData("image-data".getBytes())
                .build();
    }

    private FileResponseDTO validResponse() {
        return FileResponseDTO.builder()
                .fileId(FILE_ID)
                .fileName("product.png")
                .fileType("image/png")
                .fileData("image-data".getBytes())
                .build();
    }

    private void enqueueJson(int status, Object body) throws Exception {
        mockBackEnd.enqueue(jsonResponse(status).setBody(objectMapper.writeValueAsString(body)));
    }

    private void enqueueStatus(int status) {
        mockBackEnd.enqueue(jsonResponse(status).setBody("{\"message\":\"error\"}"));
    }

    private MockResponse jsonResponse(int status) {
        return new MockResponse()
                .setResponseCode(status)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    private RecordedRequest assertRequest(String method, String path) throws Exception {
        RecordedRequest request = mockBackEnd.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals(method, request.getMethod());
        assertEquals(path, request.getPath());
        return request;
    }
}
