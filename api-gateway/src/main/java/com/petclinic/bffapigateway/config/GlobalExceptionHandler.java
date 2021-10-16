package com.petclinic.bffapigateway.config;

import com.petclinic.bffapigateway.exceptions.GenericHttpException;
import com.petclinic.bffapigateway.exceptions.HttpErrorInfo;
import org.springframework.http.ResponseEntity;
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

    @ExceptionHandler(value = GenericHttpException.class)
    //@ResponseStatus(value = BAD_REQUEST)
    public ResponseEntity<HttpErrorInfo> resourceNotFoundException(GenericHttpException ex) {

        return ResponseEntity.status(ex.getHttpStatus())
                .body(new HttpErrorInfo(ex.getHttpStatus().value(), ex.getMessage()));
    }
}