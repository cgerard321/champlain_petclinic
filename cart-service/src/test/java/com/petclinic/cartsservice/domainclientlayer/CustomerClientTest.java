package com.petclinic.cartsservice.domainclientlayer;

import com.petclinic.cartsservice.utils.exceptions.InvalidInputException;
import com.petclinic.cartsservice.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CustomerClientTest {

    private ExchangeFunction exchangeFunction;
    private CustomerClient customerClient;

    @BeforeEach
    void setUp() {
        exchangeFunction = Mockito.mock(ExchangeFunction.class);

        WebClient webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();

        customerClient = new CustomerClient("localhost", "8080");
        try {
            var f = CustomerClient.class.getDeclaredField("webClient");
            f.setAccessible(true);
            f.set(customerClient, webClient);
        } catch (Exception ignored) {}
    }

    @Test
    void getCustomerById_success() {
        ClientResponse response = ClientResponse
                .create(org.springframework.http.HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body("{\"customerId\":\"123\"}")
                .build();

        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(response));

        StepVerifier.create(customerClient.getCustomerById("123"))
                .expectNextMatches(c -> c.getCustomerId().equals("123"))
                .verifyComplete();
    }

    @Test
    void getCustomerById_404_throwsNotFound() {
        ClientResponse response = ClientResponse.create(org.springframework.http.HttpStatus.NOT_FOUND).build();
        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(response));

        StepVerifier.create(customerClient.getCustomerById("missing"))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void getCustomerById_422_throwsInvalidInput() {
        ClientResponse response = ClientResponse.create(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY).build();
        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(response));

        StepVerifier.create(customerClient.getCustomerById("bad"))
                .expectError(InvalidInputException.class)
                .verify();
    }

    @Test
    void getCustomerById_500_throwsIllegalArgument() {
        ClientResponse response = ClientResponse.create(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(response));

        StepVerifier.create(customerClient.getCustomerById("boom"))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
