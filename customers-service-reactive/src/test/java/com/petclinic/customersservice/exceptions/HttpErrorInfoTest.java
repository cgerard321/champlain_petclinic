package com.petclinic.customersservice.exceptions;

import com.petclinic.customersservice.customersExceptions.http.HttpErrorInfo;
import org.junit.jupiter.api.Test;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class HttpErrorInfoTest {

    @Test
    void httpErrorInfoTest() {
        HttpErrorInfo error = new HttpErrorInfo(UNPROCESSABLE_ENTITY, "/", "Invalid");
        assertEquals(error.getMessage(), "Invalid");
        assertEquals(error.getPath(), "/");
        assertEquals(error.getHttpStatus(), UNPROCESSABLE_ENTITY);
        assertThat(error.getTimestamp()).isNotNull();
    }

    @Test
    void HttpErrorInfoWithNoConstructorTest(){
        HttpErrorInfo httpErrorInfo = new HttpErrorInfo();
        assertEquals(httpErrorInfo.getTimestamp(), null);
        assertEquals(httpErrorInfo.getHttpStatus(), null);
        assertEquals(httpErrorInfo.getPath(), null);
        assertEquals(httpErrorInfo.getMessage(), null);
    }

}
