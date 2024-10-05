package com.petclinic.products.utils.exceptions;

public class InvalidImageTypeException extends RuntimeException{

    public InvalidImageTypeException() {}

    public InvalidImageTypeException(String message) { super(message); }

    public InvalidImageTypeException(Throwable cause) { super(cause); }

    public InvalidImageTypeException(String message, Throwable cause) { super(message, cause); }
}
