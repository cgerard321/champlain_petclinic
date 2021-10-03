package com.petclinic.visits.utils.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

/*
* This class simply defines the InvalidInputException with all of its possible constructors. It makes sure that when
* this exception is thrown and handled, the HTTP status associated with the response is 422:UNPROCESSABLE_ENTITY.
*
* Contributors:
*   70963776+cjayneb@users.noreply.github.com
*/

@ResponseStatus(UNPROCESSABLE_ENTITY)
public class InvalidInputException extends RuntimeException{

    public InvalidInputException(){

    }

    public InvalidInputException(String message){ super(message);}

    public InvalidInputException(Throwable cause){ super(cause);}

    public InvalidInputException(String message, Throwable cause){ super(message, cause);}
}
