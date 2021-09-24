package com.petclinic.bffapigateway.domainclientlayer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AuthenticationServiceClient {
    private final WebClient.Builder webClientBuilder;
    private final String authenticationServiceUrl;

    public AuthenticationServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.authentication-service.host}") String authenticationServiceHost,
            @Value("${app.authentication-service.port}") String authenticationServicePort
    ) {
        this.webClientBuilder = webClientBuilder;
        authenticationServiceUrl = "http://" + authenticationServiceHost + ":" + authenticationServicePort + "/";
    }


}
