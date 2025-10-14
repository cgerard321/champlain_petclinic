package com.petclinic.bffapigateway.utils;


import com.petclinic.bffapigateway.exceptions.HandlerIsNullException;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;

import java.util.concurrent.ExecutionException;

@Component
public class Utility {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    public Utility(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }


    public HandlerMethod getHandler(ServerWebExchange exchange){
        HandlerMethod handler;
        try {
            handler = (HandlerMethod) requestMappingHandlerMapping.getHandler(exchange).toFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new HandlerIsNullException(e.getMessage());
        }

        if (handler == null) {
            throw new HandlerIsNullException("Handler is null, check if the endpoint is valid");
        }

        return handler;

    }


}
