package com.petclinic.bffapigateway.exceptions;

public class DuplicateTimeException extends RuntimeException{
    public DuplicateTimeException(final String message) {
        super(message);
    }
}
