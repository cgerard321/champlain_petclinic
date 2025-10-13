package com.petclinic.bffapigateway.config;

import com.petclinic.bffapigateway.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.webjars.NotFoundException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: @nariman
 * Date: 2021-10-15
 * Ticket: feat(APIG-CPC-354)
 */

@Slf4j
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

    @ExceptionHandler(value = InvalidCredentialsException.class)
    public ResponseEntity<HttpErrorInfo> invalidCredentialException(InvalidCredentialsException ex){
        return ResponseEntity.status(401).body(new HttpErrorInfo(401,ex.getMessage()));
    }


    @ExceptionHandler(value = InvalidTokenException.class)
    public ResponseEntity<HttpErrorInfo> invalidTokenException(InvalidTokenException ex) {

        return ResponseEntity.status(498)
                .body(new HttpErrorInfo(498, ex.getMessage()));
    }


    @ExceptionHandler(value = NotFoundException.class)
    public ResponseEntity<HttpErrorInfo> runtimeException(RuntimeException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new HttpErrorInfo(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(WebExchangeBindException.class)
    public Map<String, String> handleValidationExceptions(WebExchangeBindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
  

    @ExceptionHandler(value = NoTokenFoundException.class)
    public ResponseEntity<HttpErrorInfo> noTokenFoundException(NoTokenFoundException ex) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new HttpErrorInfo(HttpStatus.UNAUTHORIZED.value(), ex.getMessage()));
    }



    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<HttpErrorInfo> illegalArgumentException(IllegalArgumentException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new HttpErrorInfo(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }
    
    @ExceptionHandler(value = BadRequestException.class)
    public ResponseEntity<HttpErrorInfo> handleBadRequestException(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new HttpErrorInfo(HttpStatus.BAD_REQUEST.value(),ex.getMessage()));
    }

    @ExceptionHandler(value = InventoryNotFoundException.class)
    public ResponseEntity<HttpErrorInfo> inventoryNotFoundException(InventoryNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new HttpErrorInfo(ex.getHttpStatus().value(), ex.getMessage()));
    }
    @ExceptionHandler(value = ProductListNotFoundException.class)
    public ResponseEntity<HttpErrorInfo> productListNotFoundException(ProductListNotFoundException ex){
        return ResponseEntity.status(ex.getHttpStatus())
                .body(new HttpErrorInfo(ex.getHttpStatus().value(), ex.getMessage()));
    }
    @ExceptionHandler(value = InvalidInputsInventoryException.class)
    public ResponseEntity<HttpErrorInfo> invalidInputsInventoryException(InvalidInputsInventoryException ex){
        return ResponseEntity.status(ex.getHttpStatus())
                .body(new HttpErrorInfo(ex.getHttpStatus().value(), ex.getMessage()));
    }
    @ExceptionHandler(value = ForbiddenAccessException.class)
    public ResponseEntity<HttpErrorInfo> handleForbiddenAccessException(ForbiddenAccessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new HttpErrorInfo(HttpStatus.FORBIDDEN.value(),ex.getMessage()));
    }

    @ExceptionHandler(value = InvalidInputException.class)
    public ResponseEntity<HttpErrorInfo> handleInvalidInputException(InvalidInputException ex){
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage()));
    }


}