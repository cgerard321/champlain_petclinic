package com.petclinic.customersservice.customersExceptions.exceptions;

public class NotFoundException extends RuntimeException {

    public NotFoundException() {}

    public NotFoundException(String message) { super(message); }

}
