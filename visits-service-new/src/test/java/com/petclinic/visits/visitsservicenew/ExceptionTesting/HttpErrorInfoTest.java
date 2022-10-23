package com.petclinic.visits.visitsservicenew.ExceptionTesting;

import com.petclinic.visits.visitsservicenew.Exceptions.HttpErrorInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class HttpErrorInfoTest {

    @Test
    void httpErrorInfoUtilsTest(){
        HttpErrorInfo hei = new HttpErrorInfo("/", UNPROCESSABLE_ENTITY, "Invalid Item");

        assertEquals(hei.getHttpStatus(), UNPROCESSABLE_ENTITY);
        assertEquals(hei.getMessage(), "Invalid Item");
        assertEquals(hei.getPath(), "/");
        assertThat(hei.getTimestamp()).isNotNull();

    }



}
