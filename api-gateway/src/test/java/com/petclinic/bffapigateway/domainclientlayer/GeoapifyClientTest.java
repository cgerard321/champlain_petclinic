package com.petclinic.bffapigateway.domainclientlayer;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeoapifyClientTest {

    private MockWebServer server;
    private GeoapifyClient geoapifyClient;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        geoapifyClient = new GeoapifyClient(WebClient.builder(), server.url("/").toString(), "test-key");
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    private void enqueueJson(String body) {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(body));
    }

    @Test
    void whenAutocomplete_withResults_thenReturnCanadianSuggestions() throws InterruptedException {
        enqueueJson("""
                {"results":[
                  {"address_line1":"110 Liberty Street","city":"Toronto","state_code":"ON"},
                  {"address_line1":"1 Rue Sainte-Catherine","city":"Montreal","state_code":"QC"}
                ]}
                """);

        StepVerifier.create(geoapifyClient.autocomplete("110 Lib"))
                .expectNextMatches(s -> s.getAddress().equals("110 Liberty Street")
                        && s.getCity().equals("Toronto")
                        && s.getProvince().equals("Ontario"))
                .expectNextMatches(s -> s.getProvince().equals("Quebec"))
                .verifyComplete();

        RecordedRequest request = server.takeRequest();
        assertTrue(request.getPath().startsWith("/v1/geocode/autocomplete"));
        assertEquals("110 Lib", request.getRequestUrl().queryParameter("text"));
        assertEquals("countrycode:ca", request.getRequestUrl().queryParameter("filter"));
        assertEquals("5", request.getRequestUrl().queryParameter("limit"));
        assertEquals("test-key", request.getRequestUrl().queryParameter("apiKey"));
    }

    @Test
    void whenAutocomplete_withUnknownProvinceOrNoAddress_thenSkipThoseResults() {
        enqueueJson("""
                {"results":[
                  {"address_line1":"10 Main St","city":"Somewhere","state_code":"XX"},
                  {"city":"Laval","state_code":"QC"},
                  {"address_line1":"5 King St","city":"Halifax","state_code":"NS"}
                ]}
                """);

        StepVerifier.create(geoapifyClient.autocomplete("Main"))
                .expectNextMatches(s -> s.getProvince().equals("Nova Scotia"))
                .verifyComplete();
    }

    @Test
    void whenAutocomplete_withNoResults_thenReturnEmpty() {
        enqueueJson("{\"results\":[]}");

        StepVerifier.create(geoapifyClient.autocomplete("zzzzzz"))
                .verifyComplete();
    }

    @Test
    void whenAutocomplete_andApiIsDown_thenReturnEmpty() {
        server.enqueue(new MockResponse().setResponseCode(503));

        StepVerifier.create(geoapifyClient.autocomplete("110 Lib"))
                .verifyComplete();
    }

    @Test
    void whenAutocomplete_withoutApiKey_thenReturnEmptyWithoutCallingTheApi() {
        GeoapifyClient clientWithoutKey =
                new GeoapifyClient(WebClient.builder(), server.url("/").toString(), "");

        StepVerifier.create(clientWithoutKey.autocomplete("110 Lib"))
                .verifyComplete();

        assertEquals(0, server.getRequestCount());
    }
}