package com.petclinic.bffapigateway.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Data
@EqualsAndHashCode(callSuper = false)
@Getter
public class InvalidInputsInventoryException extends RuntimeException {

    public HttpStatus httpStatus;
    public InvalidInputsInventoryException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
