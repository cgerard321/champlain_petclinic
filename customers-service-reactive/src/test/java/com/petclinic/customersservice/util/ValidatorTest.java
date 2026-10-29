package com.petclinic.customersservice.util;

import com.petclinic.customersservice.customersExceptions.exceptions.UnprocessableEntityException;
import com.petclinic.customersservice.presentationlayer.CustomerRequestDTO;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ValidatorTest {

    private CustomerRequestDTO customerWithProvince(String province) {
        return CustomerRequestDTO.builder()
                .firstName("George")
                .lastName("Franklin")
                .address("110 W. Liberty St.")
                .city("Madison")
                .province(province)
                .telephone("6085551023")
                .build();
    }

    @Test
    void whenValidateCustomer_withValidProvince_thenReturnCustomer() {
        CustomerRequestDTO customer = customerWithProvince("Quebec");

        StepVerifier.create(Mono.just(customer).transform(Validator.validateCustomer()))
                .expectNext(customer)
                .verifyComplete();
    }

    @Test
    void whenValidateCustomer_withInvalidProvince_thenThrowUnprocessableEntity() {
        StepVerifier.create(Mono.just(customerWithProvince("Narnia")).transform(Validator.validateCustomer()))
                .expectErrorMatches(e -> e instanceof UnprocessableEntityException
                        && e.getMessage().equals("Province must be a valid Canadian province or territory"))
                .verify();
    }

    @Test
    void whenValidateCustomer_withMissingProvince_thenThrowUnprocessableEntity() {
        StepVerifier.create(Mono.just(customerWithProvince("  ")).transform(Validator.validateCustomer()))
                .expectErrorMatches(e -> e instanceof UnprocessableEntityException
                        && e.getMessage().equals("Province is required"))
                .verify();
    }
}
