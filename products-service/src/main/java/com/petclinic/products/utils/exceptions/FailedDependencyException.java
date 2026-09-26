package com.petclinic.products.utils.exceptions;

public class FailedDependencyException extends RuntimeException {
    public FailedDependencyException(String message) {
        super(message);
    }
}
