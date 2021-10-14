package com.petclinic.auth.Config;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Data
@RequiredArgsConstructor
public class HTTPErrorMessage {

    private final HttpStatus statusCode;
    private String timestamp = ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT );
    private final String message;
}
