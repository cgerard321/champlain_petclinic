package com.petclinic.customersservice.exceptions;

import com.petclinic.customersservice.customersExceptions.exceptions.InvalidInputException;
import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import com.petclinic.customersservice.customersExceptions.http.GlobalControllerExceptionHandler;
import com.petclinic.customersservice.customersExceptions.http.HttpErrorInfo;
import com.petclinic.customersservice.data.Owner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.test.context.ActiveProfiles;
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

}
