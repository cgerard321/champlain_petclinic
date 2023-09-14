package com.petclinic.bffapigateway.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ExistingVetNotFoundException extends RuntimeException{
    private HttpStatus httpStatus;
    public ExistingVetNotFoundException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;}
}
