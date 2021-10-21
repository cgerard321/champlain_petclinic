package com.petclinic.bffapigateway.domainclientlayer;

import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

public class BillServiceLientIntegrationTest {
    private BillServiceClient client;

    private MockWebServer server;

    @BeforeEach
    void setup() {
        server = new MockWebServer();
        client = new BillServiceClient(
                WebClient.builder(),
                "http://billing-service",
                "7004"
        );
        client.setHostname(server.url("/").toString());
    }

    @AfterEach
    void shutdown() throws IOException {
        this.server.shutdown();
    }
}
