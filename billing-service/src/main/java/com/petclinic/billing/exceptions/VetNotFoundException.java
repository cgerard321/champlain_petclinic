package com.petclinic.billing.exceptions;


public class VetNotFoundException extends NotFoundException {

    public VetNotFoundException(String vetId) {
        super("Vet not found with vetId: " + vetId);
    }
}