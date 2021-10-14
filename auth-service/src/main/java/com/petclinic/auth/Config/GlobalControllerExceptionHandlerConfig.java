package com.petclinic.auth.Config;

import com.petclinic.auth.Exceptions.IncorrectPasswordException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestControllerAdvice
public class GlobalControllerExceptionHandlerConfig {

    @ExceptionHandler(value = IncorrectPasswordException.class)
    @ResponseStatus(value = UNAUTHORIZED)
    public HTTPErrorMessage resourceNotFoundException(IncorrectPasswordException ex, WebRequest request) {

        return new HTTPErrorMessage(401, ex.getMessage());
    }
}
