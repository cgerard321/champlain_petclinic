package com.petclinic.bffapigateway.config;

import com.petclinic.bffapigateway.exceptions.*;
import org.springframework.http.HttpStatus;
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


    //Exception error handler for security
    @ExceptionHandler(value = GeneralSecurityException.class)
    public ResponseEntity<HttpErrorInfo> generalSecurityException(GeneralSecurityException ex) {

        return ResponseEntity.status(401)
                .body(new HttpErrorInfo(401, ex.getMessage()));
    }


    @ExceptionHandler(value = InvalidTokenException.class)
    public ResponseEntity<HttpErrorInfo> invalidTokenException(InvalidTokenException ex) {

        return ResponseEntity.status(498)
                .body(new HttpErrorInfo(498, ex.getMessage()));
    }


    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<HttpErrorInfo> runtimeException(RuntimeException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new HttpErrorInfo(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(value = NoTokenFoundException.class)
    public ResponseEntity<HttpErrorInfo> noTokenFoundException(NoTokenFoundException ex) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new HttpErrorInfo(HttpStatus.UNAUTHORIZED.value(), ex.getMessage()));
    }

}