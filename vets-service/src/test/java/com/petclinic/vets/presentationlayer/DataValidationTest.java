package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.datalayer.DataValidation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple Tests for DataValidation class
 *
 * @author Christian
 */
public class DataValidationTest {
    @Test
    @DisplayName("Verify First Name Test")
    void verifyFirstNameTest(){
        assertThat(DataValidation.verifyFirstName("  James123 ")).isEqualTo("James");
    }

    @Test
    @DisplayName("Verify Last Name Test")
    void verifyLastNameTest(){
        assertThat(DataValidation.verifyLastName("  Carter231 ")).isEqualTo("Carter");
    }

    @Test
    @DisplayName("Verify Phone Number Test")
    void verifyPhoneNumberTest(){
        assertThat(DataValidation.verifyPhoneNumber("  #3213 ")).isEqualTo("(514)-634-8276 #3213");
    }

    @Test
    @DisplayName("Verify Email Test")
    void verifyEmailTest(){
        assertThat(DataValidation.verifyEmail("  james.carter@email.com  ")).isEqualTo("james.carter@email.com");
    }
    @Test
    @DisplayName("Verify vetId Test")
    void verifyVetIdTest(){
        assertThat(DataValidation.verifyVetId(12341213)).isEqualTo(123412);
        assertThat(DataValidation.verifyVetId(1234)).isEqualTo(1234);
    }
    @Test
    @DisplayName("Verify IsActive Test")
    void verifyIsActiveTest(){
        assertThat(DataValidation.verifyIsActive(1)).isEqualTo(1);
        assertThat(DataValidation.verifyIsActive(0)).isEqualTo(0);
        assertThat(DataValidation.verifyIsActive(12312)).isEqualTo(1);
    }
}
