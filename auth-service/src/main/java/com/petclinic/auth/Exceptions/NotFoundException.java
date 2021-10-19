/**
 * Created by IntelliJ IDEA.
 *
 * User: @Trilikin21
 * Date: 2021-10-15
 * Ticket: feat(AUTH-CPC-391)
 */

package com.petclinic.auth.Exceptions;

public class NotFoundException  extends  RuntimeException{

    public NotFoundException(String message) {
        super(message);
    }
}