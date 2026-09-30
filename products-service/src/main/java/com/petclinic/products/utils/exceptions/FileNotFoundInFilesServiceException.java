package com.petclinic.products.utils.exceptions;

public class FileNotFoundInFilesServiceException
        extends FailedDependencyException {

    public FileNotFoundInFilesServiceException(String message) {
        super(message);
    }
}
