package com.petclinic.bffapigateway.config;

import com.petclinic.bffapigateway.exceptions.ExistingVetNotFoundException;
import com.petclinic.bffapigateway.exceptions.GenericHttpException;
import com.petclinic.bffapigateway.exceptions.HttpErrorInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.security.GeneralSecurityException;

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
    public ResponseEntity<HttpErrorInfo> resourceNotFoundException(GenericHttpException ex) {

        return ResponseEntity.status(ex.getHttpStatus())
                .body(new HttpErrorInfo(ex.getHttpStatus().value(), ex.getMessage()));
    }

    // existing vet not found exception
    @ExceptionHandler(value = ExistingVetNotFoundException.class)
    public ResponseEntity<HttpErrorInfo> resourceNotFoundException(ExistingVetNotFoundException ex) {

        return ResponseEntity.status(ex.getHttpStatus())
                .body(new HttpErrorInfo(ex.getHttpStatus().value(), ex.getMessage()));
    }


    //Exception error handler
    @ExceptionHandler(value = GeneralSecurityException.class)
    public ResponseEntity<HttpErrorInfo> resourceNotFoundException(GeneralSecurityException ex) {

        return ResponseEntity.status(40)
                .body(new HttpErrorInfo(500, ex.getMessage()));
    }
}