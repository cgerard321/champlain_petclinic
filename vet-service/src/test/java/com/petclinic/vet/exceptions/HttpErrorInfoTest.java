package com.petclinic.vet.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.petclinic.vet.utils.exceptions.HttpErrorInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

class HttpErrorInfoTest {

    @Test
    void httpErrorInfoUtilsTest() {
        HttpErrorInfo hei = new HttpErrorInfo(UNPROCESSABLE_ENTITY, "/", "Invalid item");

        assertEquals(hei.getMessage(), "Invalid item");
        assertEquals(hei.getPath(), "/");
        assertEquals(hei.getHttpStatus(), UNPROCESSABLE_ENTITY);
        assertThat(hei.getTimestamp()).isNotNull();
    }

}