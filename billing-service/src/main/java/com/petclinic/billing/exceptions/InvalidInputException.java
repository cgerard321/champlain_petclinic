package com.petclinic.billing.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@ResponseStatus(UNPROCESSABLE_ENTITY)
public class InvalidInputException extends RuntimeException {
    public InvalidInputException(){

    }

    public InvalidInputException(String message){
        super(message);
    }

    public InvalidInputException(Throwable cause){
        super(cause);
    }

    public InvalidInputException(String message, Throwable cause){
        super(message, cause);
    }
}
