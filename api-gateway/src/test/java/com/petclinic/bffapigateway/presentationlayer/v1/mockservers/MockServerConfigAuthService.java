package com.petclinic.bffapigateway.presentationlayer.v1.mockservers;

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

    public static final String jwtTokenForValidVet = "valid-test-token-for-valid-vet";

    public static final String jwtTokenForInvalidVetId = "valid-test-token-for-invalid-vet-id";


    public MockServerConfigAuthService() {
        this.clientAndServer = ClientAndServer.startClientAndServer(AUTH_SERVICE_SERVER_PORT);
    }

    public void registerValidateTokenForVetEndpoint() {
        mockServerClient_AuthService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/users/validate-token")
                                .withCookie("Bearer", jwtTokenForValidVet)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json("{\"token\":\"valid-test-token\",\"userId\":\"cb6701ef-22cf-465c-be59-b1ef71cd4f2e\",\"roles\":[\"VET\"]}"))
                );


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

    public void registerForgotPasswordEndpoint() {
        mockServerClient_AuthService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/users/forgot_password")
                                .withBody(json("{\"email\":\"test@example.com\",\"url\":\"http://example.com/reset-password\"}"))
                )
                .respond(
                        response()
                                .withStatusCode(200)
                );
    }

    public void registerResetPasswordEndpoint() {
        mockServerClient_AuthService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/users/reset_password")
                                .withBody(json("{\"password\":\"Cookie123!\",\"token\":\"valid-token\"}"))
                )
                .respond(
                        response()
                                .withStatusCode(200)
                );
    }

    public void registerGetAllRolesEndpoint() {
        mockServerClient_AuthService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/roles")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json("[{\"name\":\"OWNER\"}, {\"name\":\"ADMIN\"}]"))
          
                                .withBody(json("[{\"id\":\"1\", \"name\":\"ADMIN\"}, {\"id\":\"2\", \"name\":\"OWNER\"}]"))
                );
    }

    public void registerCreateRoleEndpoint() {
        mockServerClient_AuthService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/roles")
                                .withBody(json("{\"name\":\"SUPPORT\"}"))
                )
                .respond(
                        response()
                                .withStatusCode(201)
                                .withBody(json("{\"name\":\"SUPPORT\"}"))

                                .withBody(json("{\"id\":\"6\", \"name\":\"SUPPORT\"}"))
                );
    }

    public void registerUpdateUserRolesEndpoint() {
        mockServerClient_AuthService
                .when(
                        request()
                                .withMethod("PATCH")
                                .withPath("/users/e6248486-d3df-47a5-b2e0-84d31c47533a")
                                .withCookie("Bearer", jwtTokenForValidAdmin)
                                .withBody(json("{\"roles\":[\"OWNER\", \"ADMIN\"]}"))
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json("{\"userId\":\"e6248486-d3df-47a5-b2e0-84d31c47533a\",\"username\":\"Admin\",\"email\":\"admin@admin.com\",\"roles\":[{\"id\":1,\"name\":\"ADMIN\"}, {\"id\":2,\"name\":\"OWNER\"}],\"verified\":true,\"disabled\":false}"))
                );
    }

    public void stopMockServer() {
        if(clientAndServer != null)
            this.clientAndServer.stop();
    }
}
