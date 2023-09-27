package com.petclinic.bffapigateway.config;


import com.petclinic.bffapigateway.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.GeneralSecurityException;
import java.util.Arrays;

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

            Class<? extends Throwable> exClass = ex.getClass();

            if (exClass.equals(InvalidTokenException.class) || exClass.equals(NoTokenFoundException.class) || exClass.equals(GeneralSecurityException.class)) {
                status = HttpStatus.UNAUTHORIZED;
            } else if (exClass.equals(ExistingVetNotFoundException.class) || exClass.equals(HandlerIsNullException.class)) {
                status = HttpStatus.NOT_FOUND;
            } else if (exClass.equals(GenericHttpException.class)) {
                GenericHttpException error = (GenericHttpException) ex;
                status = error.getHttpStatus();
            } else if(exClass.equals(BadRequestException.class)){
                status = HttpStatus.BAD_REQUEST;
            }
            else {
                log.error("Exception not handled: {}", exClass.getSimpleName());
                status = HttpStatus.UNPROCESSABLE_ENTITY;
            }

            // Handle any other exception types here
        }


        log.error(ex.toString());
        log.error(ex.getMessage());
        log.error(ex.getLocalizedMessage());
        log.error(ex.getCause().toString());
        log.error(Arrays.toString(ex.getStackTrace()));
        log.error(Arrays.toString(ex.getSuppressed()));


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


