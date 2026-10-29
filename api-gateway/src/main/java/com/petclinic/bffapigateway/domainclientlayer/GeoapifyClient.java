package com.petclinic.bffapigateway.domainclientlayer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.petclinic.bffapigateway.dtos.Addresses.AddressSuggestionDTO;
import com.petclinic.bffapigateway.utils.CanadianProvince;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class GeoapifyClient {

    static final int MAX_SUGGESTIONS = 5;

    private final WebClient webClient;
    private final String apiKey;

    public GeoapifyClient(WebClient.Builder webClientBuilder,
                          @Value("${app.geoapify.base-url:https://api.geoapify.com}") String baseUrl,
                          @Value("${app.geoapify.api-key:}") String apiKey) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    public Flux<AddressSuggestionDTO> autocomplete(String text) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Geoapify API key is not configured, returning no address suggestions");
            return Flux.empty();
        }

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/geocode/autocomplete")
                        .queryParam("text", text)
                        .queryParam("filter", "countrycode:ca")
                        .queryParam("lang", "en")
                        .queryParam("limit", MAX_SUGGESTIONS)
                        .queryParam("format", "json")
                        .queryParam("apiKey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(GeoapifyResponse.class)
                .timeout(Duration.ofSeconds(5))
                .flatMapMany(response -> Flux.fromIterable(
                        response.getResults() == null ? List.of() : response.getResults()))
                .filter(result -> result.getAddressLine1() != null)
                .concatMap(result -> Mono.justOrEmpty(CanadianProvince.fromCode(result.getStateCode()))
                        .map(province -> AddressSuggestionDTO.builder()
                                .address(result.getAddressLine1())
                                .city(result.getCity() != null ? result.getCity() : "")
                                .province(province.getDisplayName())
                                .build()))
                .take(MAX_SUGGESTIONS)
                .onErrorResume(e -> {
                    // Don't log e.getMessage(): it contains the request URL, which contains the API key
                    log.warn("Geoapify autocomplete failed: {}", e.getClass().getSimpleName());
                    return Flux.empty();
                });
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GeoapifyResponse {
        private List<GeoapifyResult> results;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GeoapifyResult {
        @JsonProperty("address_line1")
        private String addressLine1;
        private String city;
        @JsonProperty("state_code")
        private String stateCode;
    }
}