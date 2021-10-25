package com.petclinic.visits.presentationlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.visits.datalayer.Visit;
import com.petclinic.visits.utils.exceptions.InvalidInputException;
import com.petclinic.visits.utils.exceptions.NotFoundException;
import com.petclinic.visits.utils.http.ControllerExceptionHandler;
import com.petclinic.visits.utils.http.HttpErrorInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.petclinic.visits.datalayer.Visit.visit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class UtilsPackageTests {

    @Autowired
    ControllerExceptionHandler exceptionHandler;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void test_EmptyInvalidInputException(){
        InvalidInputException ex = assertThrows(InvalidInputException.class, ()->{
            throw new InvalidInputException();
        });
        assertEquals(ex.getMessage(), null);
    }

    @Test
    void test_ThrowableOnlyInvalidInputException(){
        InvalidInputException ex = assertThrows(InvalidInputException.class, ()->{
            throw new InvalidInputException(new Throwable());
        });
        assertEquals(ex.getCause().getMessage(), null);
    }

    @Test
    void testHandlerForInvalidInputException() throws JsonProcessingException {
        Visit expectedVisit = visit().id(1).petId(1).date(new Date()).description("").practitionerId(123456).build();

        HttpErrorInfo httpErrorInfo = exceptionHandler.handleInvalidInputException(MockServerHttpRequest.post("/owners/1/pets/{petId}/visits", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(expectedVisit)), new InvalidInputException("Visit description required"));

        assertEquals(httpErrorInfo.getHttpStatus(), HttpStatus.UNPROCESSABLE_ENTITY);
        assertEquals(httpErrorInfo.getPath(), "/owners/1/pets/1/visits");
        assertEquals(httpErrorInfo.getTimeStamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")), ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")));
    }

    @Test
    void testHttpErrorInfoNullConstructor() throws JsonProcessingException {
        HttpErrorInfo httpErrorInfo = new HttpErrorInfo();

        assertEquals(httpErrorInfo.getHttpStatus(), null);
        assertEquals(httpErrorInfo.getPath(), null);
        assertEquals(httpErrorInfo.getTimeStamp(), null);
        assertEquals(httpErrorInfo.getMessage(), null);
    }

    @Test
    void testHandlerForNotFoundException() throws JsonProcessingException {
        Visit expectedVisit = visit().id(1).petId(65).date(new Date()).description("description").practitionerId(123456).build();

        HttpErrorInfo httpErrorInfo = exceptionHandler.handleNotFoundException(MockServerHttpRequest.post("/owners/1/pets/{petId}/visits", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(expectedVisit)), new NotFoundException("Pet does not exist."));

        assertEquals(httpErrorInfo.getHttpStatus(), HttpStatus.NOT_FOUND);
        assertEquals(httpErrorInfo.getMessage(), "Pet does not exist.");
    }

    @Test
    void test_EmptyNotFoundException(){
        NotFoundException ex = assertThrows(NotFoundException.class, ()->{
            throw new NotFoundException();
        });
        assertEquals(ex.getMessage(), null);
        assertEquals(ex.getCause(), null);
    }

    @Test
    void test_ThrowableOnlyNotFoundException(){
        NotFoundException ex = assertThrows(NotFoundException.class, ()->{
            throw new NotFoundException(new Throwable("message"));
        });
        assertEquals(ex.getCause().getMessage(), "message");
    }

    @Test
    void test_MessageOnlyNotFoundException(){
        NotFoundException ex = assertThrows(NotFoundException.class, ()->{
            throw new NotFoundException("message");
        });
        assertEquals(ex.getMessage(), "message");
    }

    @Test
    void test_ThrowableMessageNotFoundException(){
        NotFoundException ex = assertThrows(NotFoundException.class, ()->{
            throw new NotFoundException("message", new Throwable("message"));
        });
        assertEquals(ex.getCause().getMessage(), "message");
        assertEquals(ex.getMessage(), "message");
    }
}
