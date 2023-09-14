package com.auth.authservice.Util;


import com.auth.authservice.Util.Exceptions.*;
import io.jsonwebtoken.JwtException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

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

    @ExceptionHandler(value = ConstraintViolationException.class)
    @ResponseStatus(value = BAD_REQUEST)
    public HTTPErrorMessage constraintViolationException(ConstraintViolationException ex, WebRequest request) {

        final List<String> collect = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        return new HTTPErrorMessage(
                BAD_REQUEST.value(),
                String.join("\n", collect));
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

    @ExceptionHandler(value = JwtException.class)
    @ResponseStatus(value = BAD_REQUEST)
    public HTTPErrorMessage jwtException(JwtException ex) {

        return new HTTPErrorMessage(BAD_REQUEST.value(), ex.getMessage());
    }
}
