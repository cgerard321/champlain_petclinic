package com.petclinic.auth.Exceptions;

public class NotFoundException  extends  RuntimeException{

    public NotFoundException(String message) {
        super(message);
    }
}