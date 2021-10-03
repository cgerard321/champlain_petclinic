package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.datalayer.DataValidation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DataValidationTest {
    @Test
    @DisplayName("Verify First Name Test")
    void verifyFirstNameTest(){
        assertThat(DataValidation.verifyFirstName("  James123 ")).isEqualTo("James");
    }

}
