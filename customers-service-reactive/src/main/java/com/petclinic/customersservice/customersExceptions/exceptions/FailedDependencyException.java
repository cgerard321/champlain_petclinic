package com.petclinic.customersservice.customersExceptions.exceptions;

public class FailedDependencyException extends RuntimeException {
    public FailedDependencyException(String message) {
        super(message);
    }
}
