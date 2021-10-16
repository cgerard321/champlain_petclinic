package com.petclinic.auth.Config;

import com.petclinic.auth.Exceptions.HTTPErrorMessage;
import com.petclinic.auth.Exceptions.IncorrectPasswordException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolationException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestControllerAdvice
public class GlobalControllerExceptionHandlerConfig {

    @ExceptionHandler(value = IncorrectPasswordException.class)
    @ResponseStatus(value = UNAUTHORIZED)
    public HTTPErrorMessage incorrectPasswordException(IncorrectPasswordException ex, WebRequest request) {

        return new HTTPErrorMessage(UNAUTHORIZED.value(), ex.getMessage());
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    @ResponseStatus(value = BAD_REQUEST)
    public HTTPErrorMessage constraintViolationException(ConstraintViolationException ex, WebRequest request) {

        return new HTTPErrorMessage(BAD_REQUEST.value(), ex.getMessage().split(": ?")[1]);
    }
}
