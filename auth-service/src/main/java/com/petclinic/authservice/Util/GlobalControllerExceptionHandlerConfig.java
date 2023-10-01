package com.petclinic.authservice.Util;


import com.petclinic.authservice.Util.Exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalControllerExceptionHandlerConfig {


    @ExceptionHandler(value = IncorrectPasswordException.class)
    @ResponseStatus(value = UNAUTHORIZED)
    public HTTPErrorMessage incorrectPasswordException(IncorrectPasswordException ex, WebRequest request) {

        return new HTTPErrorMessage(UNAUTHORIZED.value(), ex.getMessage());
    }

//    @ExceptionHandler(value = MethodArgumentNotValidException.class)
//    @ResponseStatus(value = BAD_REQUEST)
//    public HTTPErrorMessage weakPasswordException(MethodArgumentNotValidException ex, WebRequest request) {
//
//        List<String> errors = ex.getBindingResult().getFieldErrors()
//                .stream().map(FieldError::getDefaultMessage).collect(Collectors.toList());
//        return new HTTPErrorMessage(BAD_REQUEST.value(), String.join(",", errors));
//
//    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<HTTPErrorMessage> weakPasswordException(MethodArgumentNotValidException ex, WebRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(FieldError::getDefaultMessage).collect(Collectors.toList());
        String errorMessage = String.join(",", errors);
        HTTPErrorMessage errorResponse = new HTTPErrorMessage(400 , errorMessage);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(value = EmailAlreadyExistsException.class)
    @ResponseStatus(value = BAD_REQUEST)
    public HTTPErrorMessage emailAlreadyExistsException(EmailAlreadyExistsException ex, WebRequest request) {

        return new HTTPErrorMessage(BAD_REQUEST.value(), ex.getMessage());
    }

    @ExceptionHandler(value = NotFoundException.class)
    @ResponseStatus(value = NOT_FOUND)
    public HTTPErrorMessage NotFoundException(NotFoundException ex, WebRequest request) {

        return new HTTPErrorMessage(404, ex.getMessage());
    }

    @ExceptionHandler(value = InvalidInputException.class)
    @ResponseStatus(value = UNPROCESSABLE_ENTITY)
    public HTTPErrorMessage resourceNotFoundException(InvalidInputException ex, WebRequest request) {

        return new HTTPErrorMessage(422, ex.getMessage());
    }

    @ExceptionHandler(value = InvalidBearerTokenException.class)
    @ResponseStatus(value = UNAUTHORIZED)
    public HTTPErrorMessage invalidBearerTokenException(InvalidBearerTokenException ex, WebRequest request) {

        return new HTTPErrorMessage(UNAUTHORIZED.value(), ex.getMessage());
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    @ResponseStatus(value = BAD_REQUEST)
    public HTTPErrorMessage illegalArgumentException(IllegalArgumentException ex) {

        return new HTTPErrorMessage(BAD_REQUEST.value(), ex.getMessage());
    }
}
