package com.petclinic.bffapigateway.utils.Security.Exceptions;

import lombok.Data;


public class InvalidTokenException extends RuntimeException{
    public InvalidTokenException(String message) {
        super(message);
    }
}
