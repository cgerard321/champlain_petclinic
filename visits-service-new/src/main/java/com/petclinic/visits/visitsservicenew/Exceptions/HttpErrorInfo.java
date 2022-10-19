package com.petclinic.visits.visitsservicenew.Exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import java.time.ZonedDateTime;

@Getter
public class HttpErrorInfo {

    private final ZonedDateTime timestamp;
    private final String path;
    private final HttpStatus httpStatus;
    private final String message;

    public HttpErrorInfo(String path, HttpStatus httpStatus, String message) {
        timestamp = ZonedDateTime.now();
        this.path = path;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
