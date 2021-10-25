package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.datalayer.DataValidation;
import com.petclinic.vets.utils.exceptions.InvalidInputException;
import com.petclinic.vets.utils.http.HttpErrorInfo;
import com.petclinic.vets.utils.http.ServiceUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;


/**
 * Simple Http Utils tests
 *
 * @author Christian
 */
public class UtilsHttpTest {

    final ServiceUtil su = new ServiceUtil("7002");

    @Test
    @DisplayName("Service Utils Test")
    void serviceUtilsTest() {

        assertNotNull(su.getServiceAddress());
    }

    @Test
    @DisplayName("Http Error Info test")
    void httpErrorInfoUtilsTest() {
        HttpErrorInfo heiEmpty = new HttpErrorInfo();
        assertEquals(heiEmpty.getMessage(), null);
        assertEquals(heiEmpty.getPath(), null);
        assertEquals(heiEmpty.getHttpStatus(), null);
        assertThat(heiEmpty.getTimestamp()).isNull();
        HttpErrorInfo hei = new HttpErrorInfo(UNPROCESSABLE_ENTITY, "/", "Invalid item");
        assertEquals(hei.getMessage(), "Invalid item");
        assertEquals(hei.getPath(), "/");
        assertEquals(hei.getHttpStatus(), UNPROCESSABLE_ENTITY);
        assertThat(hei.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Verify Last Name InvalidInputException Test")
    void verifyLastNameInvalidInputException() {
        assertThrows(InvalidInputException.class, () -> {
            DataValidation.verifyLastName("  #@$#$@$# ");
        });
    }
}
