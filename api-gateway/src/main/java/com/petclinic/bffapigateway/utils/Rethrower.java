package com.petclinic.bffapigateway.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;

/**
 * Created by IntelliJ IDEA.
 *
 * User: @Feat
 * Date: 2021-10-15
 * Ticket: feat(AUTH-CPC-354)
 */
@RequiredArgsConstructor
@Component
public class Rethrower implements Function<ClientResponse, Mono<? extends Throwable>> {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<? extends Throwable> apply(ClientResponse clientResponse) {
        return clientResponse.createException().flatMap(n ->
        {
            try {
                final Map map =
                        objectMapper.readValue(n.getResponseBodyAsString(), Map.class);
                return Mono.error(new RuntimeException(
                        map.get("message").toString()
                ));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return Mono.error(e);
            }
        });
    }
}
