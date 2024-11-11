package com.petclinic.billing.exceptions;

public class InvalidPaymentException extends RuntimeException {
    public InvalidPaymentException(String message) {
        super(message);
    }
}
