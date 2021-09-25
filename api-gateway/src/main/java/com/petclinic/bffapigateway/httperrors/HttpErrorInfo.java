package com.petclinic.bffapigateway.httperrors;

import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

public class HttpErrorInfo {
    private final ZonedDateTime timeStamp;
    private final String path;
    private final HttpStatus httpStatus;
    private final String message;

    public HttpErrorInfo(HttpStatus httpStatus, String path, String message)
    {
        timeStamp = ZonedDateTime.now();
        this.path= path;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public HttpErrorInfo() {
        timeStamp = null;
        path = null;
        httpStatus = null;
        message = null;
    }

    public ZonedDateTime getTimeStamp() {
        return this.timeStamp;
    }

    public String getPath() {
        return this.path;
    }

    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }

    public String getMessage() {
        return this.message;
    }
}
