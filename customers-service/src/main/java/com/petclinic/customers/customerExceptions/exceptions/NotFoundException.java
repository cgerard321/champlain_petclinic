package com.petclinic.customers.customerExceptions.exceptions;

public class NotFoundException extends RuntimeException{

    public NotFoundException() {}

    public NotFoundException(String message) { super(message); }
}
