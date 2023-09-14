/**
 * Created by IntelliJ IDEA.
 * <p>
 * User: @Trilikin21
 * Date: 2021-10-15
 * Ticket: feat(AUTH-CPC-391)
 */

package com.auth.authservice.Util.Exceptions;

public class InvalidInputException extends RuntimeException {

    public InvalidInputException(String message) {
        super((message));
    }

}
