package com.petclinic.vet.utils.exceptions;

public class FailedDependencyException extends RuntimeException {
    public FailedDependencyException(String message) {
        super(message);
    }
}

