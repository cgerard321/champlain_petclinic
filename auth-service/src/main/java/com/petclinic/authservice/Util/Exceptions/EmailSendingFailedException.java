package com.petclinic.authservice.Util.Exceptions;

public class EmailSendingFailedException extends RuntimeException {

    public EmailSendingFailedException(String message) {
        super(message);
    }
}
