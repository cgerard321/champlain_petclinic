package com.petclinic.bffapigateway.presentationlayer.v2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class VetProfileAuthorizationTest {

    @Autowired
    private WebTestClient webTestClient;

    private static final String VET_ENDPOINT = "/api/v2/gateway/vets";

    @ParameterizedTest
    @CsvSource({
            "ANONYMOUS, selected-vet, 401"
    })
    @DisplayName("Test vet profile update endpoint authorization for unauthenticated access")
    void protectsTheProfileUpdateEndpoint(String role, String vetId, int expectedStatus) {
        String payload = "{\"firstName\":\"John\",\"lastName\":\"Doe\"}";

        webTestClient.put()
                .uri(VET_ENDPOINT + "/" + vetId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }
}