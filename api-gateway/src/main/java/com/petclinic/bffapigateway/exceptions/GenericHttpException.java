package com.petclinic.bffapigateway.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 2021-10-15
 * Ticket: feat(APIG-CPC-354)
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class GenericHttpException extends RuntimeException {

    private HttpStatus httpStatus;

    public GenericHttpException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
