package com.petclinic.bffapigateway.config;


import com.petclinic.bffapigateway.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.GeneralSecurityException;

@Slf4j
@Component
public class GlobalServletExceptionHandler implements ErrorWebExceptionHandler {

    @SuppressWarnings("NullableProblems")
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status;
        try{
            //get the first 3 digits of the exception message for the error code
            status = HttpStatus.valueOf((Integer.parseInt(ex.getMessage().substring(0, 3))));
        }
        catch (Exception e){

            log.debug("Exception type: {}", ex.getClass().getSimpleName());

            switch (ex.getClass().getSimpleName()) {
                case "InvalidTokenException", "NoTokenFoundException", "GeneralSecurityException" ->
                        status = HttpStatus.UNAUTHORIZED;
                case "ExistingVetNotFoundException" -> status = HttpStatus.NOT_FOUND;
                case "GenericHttpException" -> {
                    GenericHttpException error = (GenericHttpException) ex;
                    status = error.getHttpStatus();
                }
                default -> {
                    log.error("Exception not handled: {}", ex.getClass().getSimpleName());
                    status = HttpStatus.UNPROCESSABLE_ENTITY;
                }
            }
            // Handle any other exception types here
        }




        if (exchange.getResponse().isCommitted()) {
            log.error("Error writing response", ex);
            return Mono.error(ex);
        }

        exchange.getResponse().setStatusCode(status);
        HttpErrorInfo errorInfo = new HttpErrorInfo(status.value(), ex.getMessage());

        exchange.getResponse().getHeaders().setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                .bufferFactory().wrap((errorInfo.toJson()).getBytes())));


    }

}


