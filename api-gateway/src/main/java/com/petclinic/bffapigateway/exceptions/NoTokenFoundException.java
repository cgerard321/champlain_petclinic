package com.petclinic.bffapigateway.exceptions;


public class NoTokenFoundException extends RuntimeException{

    public NoTokenFoundException(String message) {
        super(message);
    }
}
