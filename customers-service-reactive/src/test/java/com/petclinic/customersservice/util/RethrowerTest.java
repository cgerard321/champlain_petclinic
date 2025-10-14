package com.petclinic.customersservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.customersservice.customersExceptions.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

class RethrowerTest {

    private Rethrower rethrower;
    private ObjectMapper objectMapper;

    @Mock
    private ClientResponse clientResponse;

    @Mock
    private WebClientResponseException webClientResponseException;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        rethrower = new Rethrower(objectMapper);
    }

    @Test
    void rethrow_WithValidResponse_ShouldConvertToCustomException() {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("message", "Resource not found");
        errorMap.put("status", 404);
        
        String responseBody = "{\"message\":\"Resource not found\",\"status\":404}";
        
        when(clientResponse.createException()).thenReturn(Mono.just(webClientResponseException));
        when(webClientResponseException.getResponseBodyAsString()).thenReturn(responseBody);

        Mono<? extends Throwable> result = rethrower.rethrow(clientResponse, 
            map -> new NotFoundException((String) map.get("message")));

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof NotFoundException &&
                    throwable.getMessage().contains("Resource not found"))
                .verify();
    }

    @Test
    void rethrow_WithMalformedJson_ShouldReturnJsonProcessingException() {
        String malformedJson = "{invalid json}";
        
        when(clientResponse.createException()).thenReturn(Mono.just(webClientResponseException));
        when(webClientResponseException.getResponseBodyAsString()).thenReturn(malformedJson);

        Mono<? extends Throwable> result = rethrower.rethrow(clientResponse, 
            map -> new NotFoundException((String) map.get("message")));

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> 
                    throwable instanceof com.fasterxml.jackson.core.JsonProcessingException)
                .verify();
    }
}
