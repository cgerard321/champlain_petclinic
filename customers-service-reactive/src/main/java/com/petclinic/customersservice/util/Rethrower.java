package com.petclinic.customersservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
@Component
public class Rethrower {

    private static final Logger log = LoggerFactory.getLogger(Rethrower.class);
    private final ObjectMapper objectMapper;

    public Mono<? extends Throwable> rethrow(ClientResponse clientResponse, Function<Map, ? extends Throwable> exceptionProvider) {
        return clientResponse.createException().flatMap(n ->
        {
            try {
                final Map map =
                        objectMapper.readValue(n.getResponseBodyAsString(), Map.class);
                return Mono.error(exceptionProvider.apply(map));
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
                return Mono.error(e);
            }
        });
    }

}
