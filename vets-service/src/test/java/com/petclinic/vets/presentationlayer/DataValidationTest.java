package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.datalayer.DataValidation;
import com.petclinic.vets.utils.exceptions.InvalidInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Simple Tests for DataValidation class
 *
 * @author Christian
 */
public class DataValidationTest {
    @Test
    @DisplayName("DataValidation empty Constructor")
    void DataValidationNoArgsConstructorTest() {
        DataValidation dv = new DataValidation();
        assertEquals(dv.getClass(), DataValidation.class);
    }

    @Test
    @DisplayName("Verify First Name Test")
    void verifyFirstNameTest() {
        assertThat(DataValidation.verifyFirstName("  James123 ")).isEqualTo("James");
    }

    @Test
    @DisplayName("Verify First Name InvalidInputException Test")
    void verifyFirstNameInvalidInputException() {
        assertThrows(InvalidInputException.class, () -> {
            DataValidation.verifyFirstName("  #@$#$@$# ");
        });
    }

    @Test
    @DisplayName("Verify Last Name Test")
    void verifyLastNameTest() {
        assertThat(DataValidation.verifyLastName("  Carter231 ")).isEqualTo("Carter");
    }

    @Test
    @DisplayName("Verify Last Name InvalidInputException Test")
    void verifyLastNameInvalidInputException() {
        assertThrows(InvalidInputException.class, () -> {
            DataValidation.verifyLastName("  #@$#$@$# ");
        });
    }

    @Test
    @DisplayName("Verify Phone Number Test")
    void verifyPhoneNumberTest() {
        assertThat(DataValidation.verifyPhoneNumber("  #3213 ")).isEqualTo("(514)-634-8276 #3213");
    }

    @Test
    @DisplayName("Verify Phone Number InvalidInputException Test")
    void verifyPhoneNumberInvalidInputException() {
        assertThrows(InvalidInputException.class, () -> {
            DataValidation.verifyPhoneNumber("  sadsd  ");
        });
        assertThrows(InvalidInputException.class, () -> {
            DataValidation.verifyPhoneNumber("  #dadasda  ");
        });
    }

    @Test
    @DisplayName("Verify Email Test")
    void verifyEmailTest() {
        assertThat(DataValidation.verifyEmail("  james.carter@email.com  ")).isEqualTo("james.carter@email.com");
    }

    @Test
    @DisplayName("Verify Email InvalidInputException Test")
    void verifyEmailInvalidInputException() {
        assertThrows(InvalidInputException.class, () -> {
            DataValidation.verifyEmail("  da.email.com  ");
        });
        assertThrows(InvalidInputException.class, () -> {
            DataValidation.verifyEmail("  da.em#%@#%@.com  ");
        });
    }

    @Test
    @DisplayName("Verify Workday Test")
    void verifyWorkdayTest() {
        assertThat(DataValidation.verifyWorkday("Monday, Tuesday")).isEqualTo("Monday, Tuesday");
        assertThat(DataValidation.verifyWorkday("Monday")).isEqualTo("Monday");
    }

    @Test
    @DisplayName("Verify Workday InvalidInputException Test")
    void verifyWorkdayInvalidInputException() {
        assertThrows(InvalidInputException.class, () -> {
            DataValidation.verifyWorkday("Monday-Tuesday");
        });
        assertThrows(InvalidInputException.class, () -> {
            DataValidation.verifyWorkday("Monday| Tuesday");
        });
        assertThrows(InvalidInputException.class, () -> {
            DataValidation.verifyWorkday("$!@##$#@dafsd793924");
        });
    }

    @Test
    @DisplayName("Verify vetId Test")
    void verifyVetIdTest() {
        assertThat(DataValidation.verifyVetId(12341213)).isEqualTo(123412);
        assertThat(DataValidation.verifyVetId(123412)).isEqualTo(123412);
        assertThat(DataValidation.verifyVetId(1234)).isEqualTo(1234);
        assertThat(DataValidation.verifyVetId(0)).isGreaterThanOrEqualTo(1);
        assertThat(DataValidation.verifyVetId(0)).isLessThanOrEqualTo(999999);
    }

    @Test
    @DisplayName("Verify vetId InvalidInputException Test")
    void verifyVetIdInvalidInputException() {
        assertThrows(InvalidInputException.class, () -> {
            DataValidation.verifyVetId(-120);
        });
    }

    @Test
    @DisplayName("Verify IsActive Test")
    void verifyIsActiveTest() {
        assertThat(DataValidation.verifyIsActive(1)).isEqualTo(1);
        assertThat(DataValidation.verifyIsActive(0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Verify isActive InvalidInputException Test")
    void verifyisActiveInvalidInputException() {
        assertThrows(InvalidInputException.class, () -> {
            DataValidation.verifyIsActive(23232);
        });
        assertThrows(InvalidInputException.class, () -> {
            DataValidation.verifyIsActive(-1);
        });
    }

    @Test
    @DisplayName("Verify Speciality InvalidInputException Test")
    void verifySpecialityInvalidInputException() {
        assertThrows(InvalidInputException.class, () -> {
            DataValidation.verifySpeciality("  #@$#$@$# ");
        });
    }
}
