package com.petclinic.customers.customerExceptions.exceptions;

public class InvalidInputException extends RuntimeException {

    public InvalidInputException() {}

    public InvalidInputException(String message) { super(message); }
}

