package com.petclinic.authservice.Util.Exceptions;

public class DisabledAccountException extends RuntimeException {
    public DisabledAccountException(String message) {
        super(message);
    }
}