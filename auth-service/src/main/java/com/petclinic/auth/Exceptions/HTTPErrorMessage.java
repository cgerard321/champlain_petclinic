/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 2021-10-14
 * Ticket: feat(AUTH-CPC-388)
 */

package com.petclinic.auth.Exceptions;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Data
@RequiredArgsConstructor
public class HTTPErrorMessage {

    private final int statusCode;
    private String timestamp = ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT );
    private final String message;
}
