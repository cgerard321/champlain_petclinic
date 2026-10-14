package com.petclinic.billing.exceptions;


public class CustomerNotFoundException extends NotFoundException {

    public CustomerNotFoundException(String customerId) {
        super("Customer not found with customerId: " + customerId);
    }
}