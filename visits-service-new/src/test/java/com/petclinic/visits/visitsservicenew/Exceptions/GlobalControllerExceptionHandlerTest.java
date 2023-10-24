package com.petclinic.visits.visitsservicenew.Exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;


@SpringBootTest
@ExtendWith(SpringExtension.class)
class GlobalControllerExceptionHandlerTest {
    private GlobalControllerExceptionHandler exceptionHandler;
    private ServerHttpRequest serverHttpRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalControllerExceptionHandler();
        serverHttpRequest = mock(ServerHttpRequest.class);
    }

    @Test
    void handleNotFoundException_ReturnsHttpErrorInfo() {
        NotFoundException notFoundException = new NotFoundException("Resource not found");
        ServerHttpRequest serverHttpRequest = MockServerHttpRequest.get("/api/resource/1").build();

        HttpErrorInfo httpErrorInfo = exceptionHandler.handleNotFoundException(serverHttpRequest, notFoundException);

        assertEquals(HttpStatus.NOT_FOUND, httpErrorInfo.getHttpStatus());
        assertEquals("/api/resource/1", httpErrorInfo.getPath());
        assertEquals("Resource not found", httpErrorInfo.getMessage());
    }

    @Test
    void handleInvalidInputException_ReturnsHttpErrorInfo() {
        InvalidInputException invalidInputException = new InvalidInputException("Invalid input");
        ServerHttpRequest serverHttpRequest = MockServerHttpRequest.get("/api/resource").build();

        HttpErrorInfo httpErrorInfo = exceptionHandler.handleInvalidInputException(serverHttpRequest, invalidInputException);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, httpErrorInfo.getHttpStatus());
        assertEquals("/api/resource", httpErrorInfo.getPath());
        assertEquals("Invalid input", httpErrorInfo.getMessage());
    }

    @Test
    void handleBadRequestException_ReturnsHttpErrorInfo() {
        BadRequestException badRequestException = new BadRequestException("Bad Request");
        ServerHttpRequest serverHttpRequest = MockServerHttpRequest.get("/api/resource").build();

        HttpErrorInfo httpErrorInfo = exceptionHandler.handleBadRequestException(serverHttpRequest, badRequestException);

        assertEquals(HttpStatus.BAD_REQUEST, httpErrorInfo.getHttpStatus());
        assertEquals("/api/resource", httpErrorInfo.getPath());
        assertEquals("Bad Request", httpErrorInfo.getMessage());
    }

    @Test
    void handleDuplicateTimeException_ReturnsHttpErrorInfo() {
        // Arrange
        DuplicateTimeException duplicateTimeException = new DuplicateTimeException("A visit with the same time and practitioner already exists.");
        ServerHttpRequest serverHttpRequest = MockServerHttpRequest.get("/api/visits").build();

        // Act
        HttpErrorInfo httpErrorInfo = exceptionHandler.handleDuplicateTimeException(serverHttpRequest, duplicateTimeException);

        // Assert
        assertEquals(HttpStatus.CONFLICT, httpErrorInfo.getHttpStatus());
        assertEquals("/api/visits", httpErrorInfo.getPath());
        assertEquals("A visit with the same time and practitioner already exists.", httpErrorInfo.getMessage());
    }
}
