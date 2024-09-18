package com.petclinic.products.utils.exceptions;

public class RatingAlreadyExists extends RuntimeException {
    public RatingAlreadyExists() {}

    public RatingAlreadyExists(String message) {
        super(message);
    }

    public RatingAlreadyExists(String message, Throwable cause) {
        super(message, cause);
    }

    public RatingAlreadyExists(Throwable cause) {
        super(cause);
    }
}
