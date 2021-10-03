package com.petclinic.visits.utils.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/*
 * This class simply defines the NotFoundException with all of its possible constructors. It makes sure that when
 * this exception is thrown and handled, the HTTP status associated with the response is 404:NOT_FOUND.
 *
 * Contributors:
 *   70963776+cjayneb@users.noreply.github.com
 */

@ResponseStatus(NOT_FOUND)
public class NotFoundException extends RuntimeException{

    public NotFoundException(){

    }

    public NotFoundException(String message){ super(message);}

    public NotFoundException(Throwable cause){ super(cause);}

    public NotFoundException(String message, Throwable cause){ super(message, cause);}

}
