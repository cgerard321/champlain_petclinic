package com.petclinic.bffapigateway.exceptions;


public class HandlerIsNullException extends RuntimeException{
    public HandlerIsNullException(String message) {
        super(message);
    }
}
