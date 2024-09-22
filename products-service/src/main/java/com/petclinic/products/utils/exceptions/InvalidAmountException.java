package com.petclinic.products.utils.exceptions;

public class InvalidAmountException extends RuntimeException{

    public InvalidAmountException() {}

    public InvalidAmountException(String message) { super(message); }

    public InvalidAmountException(Throwable cause) { super(cause); }

    public InvalidAmountException(String message, Throwable cause) { super(message, cause); }
}
