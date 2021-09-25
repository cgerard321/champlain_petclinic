package com.petclinic.bffapigateway.httperrors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionController {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionController.class);

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public HttpErrorInfo handleNegativeInputException(ServerHttpRequest req, Exception ex){
        return createHttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, req, ex);
    }

    private HttpErrorInfo createHttpErrorInfo(HttpStatus httpStatus, ServerHttpRequest request, Exception ex) {

        final String path = request.getPath().pathWithinApplication().value();
        final String message = ex.getMessage();

        LOG.debug("Returning HTTP status: {}, for path: {}, message: {}", httpStatus, path, message );

        return new HttpErrorInfo(httpStatus, path, message);

    }
}
