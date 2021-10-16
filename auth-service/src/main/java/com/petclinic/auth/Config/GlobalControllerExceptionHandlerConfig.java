package com.petclinic.auth.Config;

import com.petclinic.auth.Exceptions.HTTPErrorMessage;
import com.petclinic.auth.Exceptions.IncorrectPasswordException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import java.util.List;
import java.util.stream.Collectors;

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

        final List<String> collect = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        return new HTTPErrorMessage(
                BAD_REQUEST.value(),
                collect.stream()
                        .reduce("", (a, b) -> a.concat(" ").concat(b)));
    }
}
