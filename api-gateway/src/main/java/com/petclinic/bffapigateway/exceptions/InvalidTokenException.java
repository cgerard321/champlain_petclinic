package com.petclinic.bffapigateway.exceptions;

import lombok.Data;


public class InvalidTokenException extends RuntimeException{
    public InvalidTokenException(String message) {
        super(message);
    }
}
