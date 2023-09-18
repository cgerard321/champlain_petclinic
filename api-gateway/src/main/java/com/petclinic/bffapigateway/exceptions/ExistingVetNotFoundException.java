package com.petclinic.bffapigateway.exceptions;

import lombok.Data;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Data
@Getter
public class ExistingVetNotFoundException extends RuntimeException{
    public HttpStatus httpStatus;
    public ExistingVetNotFoundException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;}
}
