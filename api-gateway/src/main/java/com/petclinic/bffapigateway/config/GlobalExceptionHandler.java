package com.petclinic.bffapigateway.config;

import com.petclinic.bffapigateway.exceptions.HttpErrorInfo;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Created by IntelliJ IDEA.
 *
 * User: @nariman
 * Date: 2021-10-15
 * Ticket: feat(APIG-CPC-354)
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = IllegalArgumentException.class)
    @ResponseStatus(value = BAD_REQUEST)
    public HttpErrorInfo resourceNotFoundException(IllegalArgumentException ex) {

        return new HttpErrorInfo(400, ex.getMessage());
    }
}