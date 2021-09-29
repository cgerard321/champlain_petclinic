package com.petclinic.vets.utils.exceptions;

public class UnavailableServiceException extends RuntimeException{

    public UnavailableServiceException(){}

    public UnavailableServiceException(String message){super(message);}

    public UnavailableServiceException(Throwable cause){super(cause);}

    public UnavailableServiceException(String message, Throwable cause){super(message, cause);}
}
