package com.petclinic.bffapigateway.exceptions;

public class BadRequestException extends RuntimeException{

    public BadRequestException(String message){ super(message);}
}
