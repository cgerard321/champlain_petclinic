package com.petclinic.products.utils.exceptions;


public class ProductInBundleConflictException extends RuntimeException {

    public ProductInBundleConflictException() {}

    public ProductInBundleConflictException(String message) {
        super(message);
    }

    public ProductInBundleConflictException(Throwable cause) { super(cause); }

    public ProductInBundleConflictException(String message, Throwable cause) { super(message, cause); }

}
