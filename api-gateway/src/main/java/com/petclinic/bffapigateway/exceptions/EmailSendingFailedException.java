package com.petclinic.bffapigateway.exceptions;

/**
 * Created by IntelliJ IDEA.
 * <p>
 * User: @Fube
 * Date: 2021-10-16
 * Ticket: feat(AUTH-CPC-460)
 */
public class EmailSendingFailedException extends RuntimeException {

    public EmailSendingFailedException(String message) {
        super(message);
    }
}
