package com.petclinic.authservice.Util.Exceptions;

/**
 * Created by IntelliJ IDEA.
 * <p>
 * User: @Fube
 * Date: 2021-10-16
 * Ticket: feat(AUTH-CPC-460)
 */
public class EmailSendingException extends RuntimeException {

    public EmailSendingException(String message) {
        super(message);
    }
}
