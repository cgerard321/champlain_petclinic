package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.datalayer.Specialty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Christian Chitanu
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 * This is a Test class used to test the constructors, accessors and mutators of the Sepecilty entity
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class SpecialtyEntityTest {

    @Test
    @DisplayName("Specialty AllArgsConstructor Test")
    void allArgsConstructorTest() {
        Specialty specialty = new Specialty(1, 111111, "Surgeon");
        assertEquals(specialty.getId(), 1);
        assertEquals(specialty.getSpecialtyId(), 111111);
        assertEquals(specialty.getName(), "Surgeon");
    }

    @Test
    @DisplayName("Specialty NoArgsConstructor Test")
    void noArgsConstructorTest() {
        Specialty specialty = new Specialty();
        assertEquals(specialty.getId(), null);
        assertEquals(specialty.getSpecialtyId(), null);
        assertEquals(specialty.getName(), null);
    }

    @Test
    @DisplayName("Specialty Setter Test")
    void setterTest() {
        Specialty specialty = new Specialty();
        specialty.setId(1);
        specialty.setSpecialtyId(111111);
        specialty.setName("Surgeon");
        assertEquals(specialty.getId(), 1);
        assertEquals(specialty.getSpecialtyId(), 111111);
        assertEquals(specialty.getName(), "Surgeon");
    }

}
