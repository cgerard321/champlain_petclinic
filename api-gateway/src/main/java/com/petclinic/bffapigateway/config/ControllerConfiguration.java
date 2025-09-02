package com.petclinic.bffapigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.config.WebFluxConfigurer;

public class ControllerConfiguration implements WebFluxConfigurer {

    @Value("${frontend.url}")
    private String frontendOrigin;

    @Override
    public void addCorsMappings(org.springframework.web.reactive.config.CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                // Remove last slash if present
                .allowedOrigins(frontendOrigin)
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
