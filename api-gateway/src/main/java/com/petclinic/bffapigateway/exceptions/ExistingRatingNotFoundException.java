package com.petclinic.bffapigateway.exceptions;

import lombok.Data;
import lombok.Getter;
import org.springframework.http.HttpStatus;


public class ExistingRatingNotFoundException extends RuntimeException{
    public ExistingRatingNotFoundException() {}

    public ExistingRatingNotFoundException(String message) { super(message); }

    public ExistingRatingNotFoundException(Throwable cause) { super(cause); }

    public ExistingRatingNotFoundException(String message, Throwable cause) { super(message, cause); }
}
