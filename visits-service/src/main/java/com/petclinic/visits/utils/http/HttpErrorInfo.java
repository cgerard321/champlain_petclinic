package com.petclinic.visits.utils.http;

import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

/*
 * This class defines the object that our controller will send back to the client when an error occurs. It contains all
 * the necessary information for the error to be clear for the client.
 *
 * Contributors:
 *   70963776+cjayneb@users.noreply.github.com
 */

public class HttpErrorInfo {

    private final ZonedDateTime timeStamp;
    private final String path;
    private final HttpStatus httpStatus;
    private final String message;

    public HttpErrorInfo(HttpStatus httpStatus, String path, String message) {
        this.timeStamp = ZonedDateTime.now();
        this.path = path;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpErrorInfo() {
        this.httpStatus = null;
        this.timeStamp = null;
        this.path = null;
        this.message = null;
    }

    public ZonedDateTime getTimeStamp() {
        return timeStamp;
    }

    public String getPath() {
        return path;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }

}
