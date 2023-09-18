package com.petclinic.bffapigateway.config;


import com.petclinic.bffapigateway.exceptions.HttpErrorInfo;
import com.petclinic.bffapigateway.exceptions.InvalidTokenException;
import com.petclinic.bffapigateway.exceptions.NoTokenFoundException;
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
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;

        if (ex instanceof InvalidTokenException) {
            status = HttpStatus.UNAUTHORIZED;
        }
       else if(ex instanceof NoTokenFoundException){
            status = HttpStatus.UNAUTHORIZED;
       }
       else if(ex instanceof RuntimeException){
            status = HttpStatus.BAD_REQUEST;
       }
       else if(ex instanceof GeneralSecurityException){
            status = HttpStatus.UNAUTHORIZED;
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


