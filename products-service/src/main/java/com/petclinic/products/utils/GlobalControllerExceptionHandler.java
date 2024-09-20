package com.petclinic.products.utils;

import com.petclinic.products.utils.exceptions.InvalidInputException;
import com.petclinic.products.utils.exceptions.NotFoundException;
import com.petclinic.products.utils.exceptions.RatingAlreadyExists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

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


    private HttpErrorInfo createHttpErrorInfo(HttpStatus httpStatus, ServerHttpRequest request, Exception ex) {
        final String path = request.getPath().value();
        // final String path = request.getPath().pathWithinApplication().value();
        final String message = ex.getMessage();
        log.debug("message is: " + message);

        log.debug("Returning HTTP status: {} for path: {}, message: {}", httpStatus, path, message);

        return new HttpErrorInfo(httpStatus, path, message);
    }
}