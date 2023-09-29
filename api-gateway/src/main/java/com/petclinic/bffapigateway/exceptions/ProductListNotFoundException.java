package com.petclinic.bffapigateway.exceptions;

import lombok.Data;
import lombok.Getter;
import org.springframework.http.HttpStatus;
@Data
@Getter
public class ProductListNotFoundException extends RuntimeException{

    public HttpStatus httpStatus;
    public ProductListNotFoundException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
