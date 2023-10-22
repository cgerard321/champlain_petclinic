package com.petclinic.visits.visitsservicenew.Exceptions;

public class DuplicateTimeException extends RuntimeException{
    public DuplicateTimeException(final String message) {
        super(message);
    }
}
