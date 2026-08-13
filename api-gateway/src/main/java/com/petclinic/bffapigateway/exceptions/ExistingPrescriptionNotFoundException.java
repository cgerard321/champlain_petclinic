package com.petclinic.bffapigateway.exceptions;

import org.springframework.http.HttpStatus;

public class ExistingPrescriptionNotFoundException extends RuntimeException {
    public ExistingPrescriptionNotFoundException(String message) {
        super(message);
    }

    public HttpStatus httpStatus;
    public ExistingPrescriptionNotFoundException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;}
}
