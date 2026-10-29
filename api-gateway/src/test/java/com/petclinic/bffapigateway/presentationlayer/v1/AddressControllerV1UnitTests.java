package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.config.GlobalExceptionHandler;
import com.petclinic.bffapigateway.domainclientlayer.GeoapifyClient;
import com.petclinic.bffapigateway.dtos.Addresses.AddressSuggestionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// No Spring context on purpose: bindToController keeps this test out of the shared
// test context cache, so it doesn't push another context out (which shuts down the
// shared Netty event loop used by the integration tests).
class AddressControllerV1UnitTests {

    private GeoapifyClient geoapifyClient;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        geoapifyClient = mock(GeoapifyClient.class);
        client = WebTestClient.bindToController(new AddressControllerV1(geoapifyClient))
                .controllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void whenAutocomplete_withValidQuery_thenReturnSuggestions() {
        AddressSuggestionDTO suggestion = AddressSuggestionDTO.builder()
                .address("110 Liberty Street").city("Toronto").province("Ontario").build();
        when(geoapifyClient.autocomplete("110 Lib")).thenReturn(Flux.just(suggestion));

        client.get()
                .uri(uriBuilder -> uriBuilder.path("/api/gateway/addresses/autocomplete")
                        .queryParam("query", "110 Lib").build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AddressSuggestionDTO.class)
                .value(list -> {
                    assertEquals(1, list.size());
                    assertEquals("Ontario", list.get(0).getProvince());
                });

        verify(geoapifyClient, times(1)).autocomplete("110 Lib");
    }

    @Test
    void whenAutocomplete_withNoSuggestions_thenReturnEmptyList() {
        when(geoapifyClient.autocomplete(anyString())).thenReturn(Flux.empty());

        client.get()
                .uri(uriBuilder -> uriBuilder.path("/api/gateway/addresses/autocomplete")
                        .queryParam("query", "zzzzzz").build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AddressSuggestionDTO.class)
                .hasSize(0);
    }

    @Test
    void whenAutocomplete_withQueryShorterThan3Characters_thenReturnBadRequest() {
        client.get()
                .uri(uriBuilder -> uriBuilder.path("/api/gateway/addresses/autocomplete")
                        .queryParam("query", "  ab  ").build())
                .exchange()
                .expectStatus().isBadRequest();

        verifyNoInteractions(geoapifyClient);
    }
}
