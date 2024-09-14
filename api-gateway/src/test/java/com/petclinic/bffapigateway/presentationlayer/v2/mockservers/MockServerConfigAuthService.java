package com.petclinic.bffapigateway.presentationlayer.v2.mockservers;

import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

public class MockServerConfigAuthService {

    private static final Integer AUTH_SERVICE_SERVER_PORT = 7005;

    private final ClientAndServer clientAndServer;

    private final MockServerClient mockServerClient_AuthService = new MockServerClient("localhost", AUTH_SERVICE_SERVER_PORT);

    public static final String jwtTokenForValidOwnerId = "valid-test-token-for-valid-owner-id";

    public static final String jwtTokenForInvalidOwnerId = "valid-test-token-for-invalid-owner-id";

    public static final String jwtTokenForValidAdmin = "valid-test-token-for-valid-admin";



    public MockServerConfigAuthService() {
        this.clientAndServer = ClientAndServer.startClientAndServer(AUTH_SERVICE_SERVER_PORT);
    }

    public void registerValidateTokenForOwnerEndpoint() {
        mockServerClient_AuthService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/users/validate-token")
                                .withCookie("Bearer", jwtTokenForValidOwnerId)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json("{\"token\":\"valid-test-token\",\"userId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\",\"roles\":[\"OWNER\"]}"))
                );

        mockServerClient_AuthService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/users/validate-token")
                                .withCookie("Bearer", jwtTokenForInvalidOwnerId)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json("{\"token\":\"valid-test-token\",\"userId\":\"invalid-owner-id\",\"roles\":[\"OWNER\"]}"))
                );
    }

    public void registerValidateTokenForAdminEndpoint() {
        mockServerClient_AuthService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/users/validate-token")
                                .withCookie("Bearer", jwtTokenForValidAdmin)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json("{\"token\":\"valid-test-token\",\"userId\":\"cb6701ef-22cf-465c-be59-b1ef71cd4f2e\",\"roles\":[\"ADMIN\"]}"))
                );
    }

    public void stopMockServer() {
        if(clientAndServer != null)
            this.clientAndServer.stop();
    }
}
