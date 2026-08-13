package com.petclinic.visits.visitsservicenew.Exceptions;

public class FailedDependencyException extends RuntimeException {
    public FailedDependencyException(String message) {
        super(message);
    }
}
