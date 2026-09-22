package com.petclinic.billing.exceptionshandling.exceptions;

public class InvalidPaymentException extends RuntimeException {
    public InvalidPaymentException(String message) {
        super(message);
    }
}
