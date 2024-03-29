package com.petclinic.bffapigateway.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 2021-10-15
 * Ticket: feat(APIG-CPC-354)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class HttpErrorInfo {

    private final int statusCode;
    private String timestamp = ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT );
    private final String message;


    public String toJson() {
        return "{" +
                "\"statusCode\":" + "\""+statusCode + "\"," +
                "\"timestamp\":" +"\""+ timestamp + "\"," +
                "\"message\":" + "\"" +message + '\"' +
                '}';
    }
}
