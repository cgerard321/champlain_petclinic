package com.petclinic.products.utils;

import com.petclinic.products.utils.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler {

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public HttpErrorInfo handleNotFoundException(ServerHttpRequest request, Exception ex) {
        return createHttpErrorInfo(NOT_FOUND, request, ex);
    }

    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidInputException.class)
    public HttpErrorInfo handleInvalidInputException(ServerHttpRequest request, Exception ex) {
        return createHttpErrorInfo(UNPROCESSABLE_ENTITY, request, ex);
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(RatingAlreadyExists.class)
    public HttpErrorInfo handleRatingAlreadyExists(ServerHttpRequest request, Exception ex){
        return createHttpErrorInfo(BAD_REQUEST, request, ex);
    }

    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidAmountException.class)
    public HttpErrorInfo handleInvalidAmountException(ServerHttpRequest request, Exception ex) {
        return createHttpErrorInfo(UNPROCESSABLE_ENTITY, request, ex);
    }

    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidImageTypeException.class)
    public HttpErrorInfo handleInvalidImageTypeException(ServerHttpRequest request, Exception ex) {
        return createHttpErrorInfo(UNPROCESSABLE_ENTITY, request, ex);
    }

    @ResponseStatus(CONFLICT)
    @ExceptionHandler(ProductInBundleConflictException.class)
    public HttpErrorInfo handleProductInBundleConflictException(ServerHttpRequest request, Exception ex) {
        return createHttpErrorInfo(CONFLICT, request, ex);
    }


    private HttpErrorInfo createHttpErrorInfo(HttpStatus httpStatus, ServerHttpRequest request, Exception ex) {
        final String path = request.getPath().value();
        // final String path = request.getPath().pathWithinApplication().value();
        final String message = ex.getMessage();
        log.debug("message is: " + message);

        log.debug("Returning HTTP status: {} for path: {}, message: {}", httpStatus, path, message);

        return new HttpErrorInfo(httpStatus, path, message);
    }
}