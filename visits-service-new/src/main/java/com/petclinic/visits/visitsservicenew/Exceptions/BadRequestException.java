package com.petclinic.visits.visitsservicenew.Exceptions;

public class BadRequestException extends RuntimeException{
    public BadRequestException(String message){ super(message);}
}
