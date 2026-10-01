package com.petclinic.products.utils.exceptions;

public class ProductTypeInUseException extends RuntimeException {
    public ProductTypeInUseException(String message) {
        super(message);
    }
}
