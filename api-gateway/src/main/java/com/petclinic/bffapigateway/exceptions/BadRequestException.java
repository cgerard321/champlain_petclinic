package com.petclinic.bffapigateway.exceptions;

public class BadRequestException extends RuntimeException{

    public BadRequestException(){}
    public BadRequestException(String message){ super(message);}
    public BadRequestException(Throwable cause){ super(cause); }
    public BadRequestException(String message, Throwable cause){
        super(message, cause);
    }
}
