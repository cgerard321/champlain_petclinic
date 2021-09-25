package com.petclinic.bffapigateway.httperrors;

public class NegativeIdException extends Exception{
    public NegativeIdException()
    {}

    public NegativeIdException(String message)
    {
        super(message);
    }

    public NegativeIdException(Throwable cause)
    {
        super(cause);
    }

    public NegativeIdException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
