package com.petclinic.bffapigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.util.Arrays;

@Configuration
public class ControllerConfiguration implements WebFluxConfigurer {

    @Value("${frontend.url}")
    private String frontendOrigin;

    @Override
    public void addCorsMappings(org.springframework.web.reactive.config.CorsRegistry registry) {
        String[] origins = Arrays.stream(frontendOrigin.split(","))
                .map(String::trim)
                .toArray(String[]::new);

        registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                .allowedOrigins(origins)
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
