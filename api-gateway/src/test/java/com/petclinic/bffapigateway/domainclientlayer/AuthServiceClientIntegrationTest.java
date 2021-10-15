package com.petclinic.bffapigateway.domainclientlayer;

import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 2021-10-15
 * Ticket: feat(APIG-CPC-354)
 */
public class AuthServiceClientIntegrationTest {

    private AuthServiceClient authServiceClient;
    private MockWebServer server;

    @BeforeEach
    void setup() {

        server = new MockWebServer();
        authServiceClient = new AuthServiceClient(
                WebClient.builder(),
                "http://auth-service",
                "1234"
        );
    }

    @AfterEach
    void shutdown() throws IOException {
        server.shutdown();
    }
}
