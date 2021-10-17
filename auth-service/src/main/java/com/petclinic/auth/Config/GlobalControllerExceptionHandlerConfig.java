package com.petclinic.auth.Config;

import com.petclinic.auth.Exceptions.HTTPErrorMessage;
import com.petclinic.auth.Exceptions.IncorrectPasswordException;
import com.petclinic.auth.Exceptions.InvalidInputException;
import com.petclinic.auth.Exceptions.NotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalControllerExceptionHandlerConfig {


    @ExceptionHandler(value = IncorrectPasswordException.class)
    @ResponseStatus(value = UNAUTHORIZED)
    public HTTPErrorMessage resourceNotFoundException(IncorrectPasswordException ex, WebRequest request) {

        return new HTTPErrorMessage(401, ex.getMessage());
    }

    @ExceptionHandler(value = NotFoundException.class)
    @ResponseStatus(value = NOT_FOUND)
    public HTTPErrorMessage NotFoundException(NotFoundException ex, WebRequest request) {

        return new HTTPErrorMessage(NOT_FOUND.value(), ex.getMessage());
    }

    @ExceptionHandler(value = InvalidInputException.class)
    @ResponseStatus(value = UNPROCESSABLE_ENTITY)
    public HTTPErrorMessage resourceNotFoundException(InvalidInputException ex, WebRequest request) {

        return new HTTPErrorMessage(UNPROCESSABLE_ENTITY.value(), ex.getMessage());
    }
}
