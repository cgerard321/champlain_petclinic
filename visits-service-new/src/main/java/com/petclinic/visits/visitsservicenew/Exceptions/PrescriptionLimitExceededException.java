package com.petclinic.visits.visitsservicenew.Exceptions;

public class PrescriptionLimitExceededException extends RuntimeException {
    public PrescriptionLimitExceededException(String message) {
        super(message);
    }
}