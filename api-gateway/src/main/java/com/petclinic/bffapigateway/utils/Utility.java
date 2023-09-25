package com.petclinic.bffapigateway.utils;


import org.springframework.http.server.reactive.ServerHttpRequest;

public class Utility {
    public static String getSiteURL(ServerHttpRequest request) {
        return request.getURI().toString();
    }
}
