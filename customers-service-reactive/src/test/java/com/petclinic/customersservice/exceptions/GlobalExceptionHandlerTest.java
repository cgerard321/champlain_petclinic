package com.petclinic.customersservice.exceptions;

import com.petclinic.customersservice.customersExceptions.exceptions.BadRequestException;
import com.petclinic.customersservice.customersExceptions.exceptions.InvalidInputException;
import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.customersExceptions.exceptions.UnprocessableEntityException;
import com.petclinic.customersservice.customersExceptions.http.GlobalControllerExceptionHandler;
import com.petclinic.customersservice.customersExceptions.http.HttpErrorInfo;
import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.domainclientlayer.FilesServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class GlobalExceptionHandlerTest {

    @Autowired
    GlobalControllerExceptionHandler exceptionHandler;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private FilesServiceClient filesServiceClient;

    @Test
    void HandleNotFoundExceptionTest() throws JsonProcessingException {
        Owner newOwner = Owner.builder()
                .id("1")
                .ownerId("ownerId-123")
                .address("Address-1")
                .firstName("Wael")
                .lastName("Osman")
                .city("City-1")
                .telephone("1234567890")
                .build();

        HttpErrorInfo httpErrorInfo = exceptionHandler.handleNotFoundException(MockServerHttpRequest.post("/owners", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(newOwner)), new NotFoundException("Owner not found."));

        assertEquals(httpErrorInfo.getHttpStatus(), HttpStatus.NOT_FOUND);
        assertEquals(httpErrorInfo.getMessage(), "Owner not found.");
    }

    @Test
    void HandleInvalidInputExceptionTest() throws JsonProcessingException {
        Owner newOwner = Owner.builder()
                .id("1")
                .ownerId("ownerId-123")
                .address("Address-1")
                .firstName("Wael")
                .lastName("Osman")
                .city("City-1")
                .telephone("1234567890")
                .build();

        HttpErrorInfo httpErrorInfo = exceptionHandler.handleInvalidInputException(MockServerHttpRequest.post("/owners", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(newOwner)), new InvalidInputException("Owner doesn't exist."));

        assertEquals(httpErrorInfo.getHttpStatus(), HttpStatus.UNPROCESSABLE_ENTITY);
        assertEquals(httpErrorInfo.getPath(), "/owners");
        assertEquals(httpErrorInfo.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")), ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")));
    }


    @Test
    void handleNotFoundException_ForPetTypePagination_ShouldReturnNotFoundStatus() {
        try {
            // Arrange
            NotFoundException exception = new NotFoundException("Pet type not found with id: non-existent-id");
            ServerHttpRequest request = MockServerHttpRequest.get("/owners/petTypes/pet-types-pagination").build();

            // Act
            HttpErrorInfo result = exceptionHandler.handleNotFoundException(request, exception);

            // Assert
            assertNotNull(result);
            assertEquals(HttpStatus.NOT_FOUND, result.getHttpStatus());
            assertEquals("/owners/petTypes/pet-types-pagination", result.getPath());
            assertEquals("Pet type not found with id: non-existent-id", result.getMessage());
            assertNotNull(result.getTimestamp());

        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void handleInvalidInputException_ForPetTypePagination_ShouldReturnUnprocessableEntityStatus() {
        try {
            // Arrange
            InvalidInputException exception = new InvalidInputException("Invalid page size: -1");
            ServerHttpRequest request = MockServerHttpRequest.get("/owners/petTypes/pet-types-pagination").build();

            // Act
            HttpErrorInfo result = exceptionHandler.handleInvalidInputException(request, exception);

            // Assert
            assertNotNull(result);
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.getHttpStatus());
            assertEquals("/owners/petTypes/pet-types-pagination", result.getPath());
            assertEquals("Invalid page size: -1", result.getMessage());
            assertNotNull(result.getTimestamp());

        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void handleNotFoundException_ForPetTypeCount_ShouldReturnNotFoundStatus() {
        try {
            // Arrange
            NotFoundException exception = new NotFoundException("No pet types found");
            ServerHttpRequest request = MockServerHttpRequest.get("/owners/petTypes/pet-types-count").build();

            // Act
            HttpErrorInfo result = exceptionHandler.handleNotFoundException(request, exception);

            // Assert
            assertNotNull(result);
            assertEquals(HttpStatus.NOT_FOUND, result.getHttpStatus());
            assertEquals("/owners/petTypes/pet-types-count", result.getPath());
            assertEquals("No pet types found", result.getMessage());
            assertNotNull(result.getTimestamp());

        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void handleInvalidInputException_ForPetTypeFilteredCount_ShouldReturnUnprocessableEntityStatus() {
        try {
            // Arrange
            InvalidInputException exception = new InvalidInputException("Invalid filter parameters");
            ServerHttpRequest request = MockServerHttpRequest.get("/owners/petTypes/pet-types-filtered-count").build();

            // Act
            HttpErrorInfo result = exceptionHandler.handleInvalidInputException(request, exception);

            // Assert
            assertNotNull(result);
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.getHttpStatus());
            assertEquals("/owners/petTypes/pet-types-filtered-count", result.getPath());
            assertEquals("Invalid filter parameters", result.getMessage());
            assertNotNull(result.getTimestamp());

        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void handleBadRequestException_WithValidRequest_ShouldReturnBadRequestStatus() {
        BadRequestException exception = new BadRequestException("Invalid file format");
        ServerHttpRequest request = MockServerHttpRequest.post("/files").build();

        HttpErrorInfo result = exceptionHandler.handleBadRequestException(request, exception);

        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getHttpStatus());
        assertEquals("/files", result.getPath());
        assertEquals("Invalid file format", result.getMessage());
        assertNotNull(result.getTimestamp());
    }

    @Test
    void handleUnprocessableEntityException_WithValidRequest_ShouldReturnUnprocessableEntityStatus() {
        UnprocessableEntityException exception = new UnprocessableEntityException("File data cannot be processed");
        ServerHttpRequest request = MockServerHttpRequest.post("/files").build();

        HttpErrorInfo result = exceptionHandler.handleUnprocessableEntityException(request, exception);

        assertNotNull(result);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.getHttpStatus());
        assertEquals("/files", result.getPath());
        assertEquals("File data cannot be processed", result.getMessage());
        assertNotNull(result.getTimestamp());
    }

    @Test
    void handleRuntimeException_WithValidRequest_ShouldReturnInternalServerErrorStatus() {
        RuntimeException exception = new RuntimeException("Unexpected server error");
        ServerHttpRequest request = MockServerHttpRequest.get("/owners").build();

        HttpErrorInfo result = exceptionHandler.handleRuntimeException(request, exception);

        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getHttpStatus());
        assertEquals("/owners", result.getPath());
        assertEquals("Unexpected server error", result.getMessage());
        assertNotNull(result.getTimestamp());
    }


}
