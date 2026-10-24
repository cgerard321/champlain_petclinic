package com.petclinic.bffapigateway.presentationlayer.v1;

import com.petclinic.bffapigateway.domainclientlayer.GeoapifyClient;
import com.petclinic.bffapigateway.dtos.Addresses.AddressSuggestionDTO;
import com.petclinic.bffapigateway.utils.Security.Filters.CsrfFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.IsUserFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.JwtTokenFilter;
import com.petclinic.bffapigateway.utils.Security.Filters.RoleFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@WebFluxTest(
        controllers = AddressControllerV1.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtTokenFilter.class, RoleFilter.class, IsUserFilter.class, CsrfFilter.class}
        )
)
@AutoConfigureWebTestClient
class AddressControllerV1UnitTests {

    @Autowired
    private WebTestClient client;

    @MockBean
    private GeoapifyClient geoapifyClient;

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