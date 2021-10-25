package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.datalayer.Specialty;
import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Christian Chitanu
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 * This is a Test class used to test the constructors, accessors and mutators of the Vet entity
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class VetEntityTest {
    @Test
    @DisplayName("Vet AllArgsConstructor Test")
    void allArgsConstructorTest() {
        Vet vet =
                new Vet(
                        1,
                        874130
                        , "James"
                        , "Carter"
                        , "carter.james@email.com"
                        , "2384"
                        , null
                        , "Practicing since 3 years"
                        , "Monday, Tuesday, Friday"
                        , 1
                        , new HashSet<Specialty>() {{
                    add(new Specialty(1, 111111, "Surgeon"));
                }}
                );
        assertEquals(vet.getId(), 1);
        assertEquals(vet.getVetId(), 874130);
        assertEquals(vet.getFirstName(), "James");
        assertEquals(vet.getLastName(), "Carter");
        assertEquals(vet.getEmail(), "carter.james@email.com");
        assertEquals(vet.getPhoneNumber(), "2384");
        assertEquals(vet.getResume(), "Practicing since 3 years");
        assertEquals(vet.getWorkday(), "Monday, Tuesday, Friday");
        assertEquals(vet.getIsActive(), 1);
        assertEquals(vet.getNrOfSpecialties(), 1);
        assertEquals(vet.getSpecialties().get(0).getName(), "Surgeon");

    }

    @Test
    @DisplayName("Vet NoArgsConstructor Test")
    void noArgsConstructorTest() {
        Vet vet = new Vet();
        assertEquals(vet.getId(), null);
        assertEquals(vet.getVetId(), null);
        assertEquals(vet.getFirstName(), null);
        assertEquals(vet.getLastName(), null);
        assertEquals(vet.getEmail(), null);
        assertEquals(vet.getPhoneNumber(), null);
        assertEquals(vet.getResume(), null);
        assertEquals(vet.getWorkday(), null);
        assertEquals(vet.getIsActive(), null);
    }

    @Test
    @DisplayName("Vet Setter Test")
    void setterTest() {
        Vet vet = new Vet();
        vet.setId(1);
        vet.setVetId(874130);
        vet.setFirstName("James");
        vet.setLastName("Carter");
        vet.setEmail("carter.james@email.com");
        vet.setPhoneNumber("2384");
        vet.setResume("Practicing since 3 years");
        vet.setWorkday("Monday, Tuesday, Friday");
        vet.setIsActive(1);
        vet.addSpecialty(new Specialty(1, 111111, "Surgeon"));
        assertEquals(vet.getId(), 1);
        assertEquals(vet.getVetId(), 874130);
        assertEquals(vet.getFirstName(), "James");
        assertEquals(vet.getLastName(), "Carter");
        assertEquals(vet.getEmail(), "carter.james@email.com");
        assertEquals(vet.getPhoneNumber(), "(514)-634-8276 #2384");
        assertEquals(vet.getResume(), "Practicing since 3 years");
        assertEquals(vet.getWorkday(), "Monday, Tuesday, Friday");
        assertEquals(vet.getIsActive(), 1);
        assertEquals(vet.getNrOfSpecialties(), 1);
        assertEquals(vet.getSpecialties().get(0).getName(), "Surgeon");
    }

    @Test
    @DisplayName("Vet toString() Test")
    void VetToStringTest() {
        Vet vet =
                new Vet(
                        1,
                        874130
                        , "James"
                        , "Carter"
                        , "carter.james@email.com"
                        , "2384"
                        , null
                        , "Practicing since 3 years"
                        , "Monday, Tuesday, Friday"
                        , 1
                        , new HashSet<Specialty>() {{
                    add(new Specialty(1, 111111, "Surgeon"));
                }}
                );
        assertEquals(vet.toString(), "[" + "Vet@" + Integer.toHexString(System.identityHashCode(vet)) + " id = 1, firstName = 'James', lastName = 'Carter', email = 'carter.james@email.com', phoneNumber = '2384', resume = 'Practicing since 3 years', workday = 'Monday, Tuesday, Friday']");
    }

    @Test
    @DisplayName("Vet AllArgsConstructor Test")
    void allArgsConstructorDTOTest() {
        VetDTO vet =
                new VetDTO(
                        874130
                        , "James"
                        , "Carter"
                        , "carter.james@email.com"
                        , "2384"
                        , null
                        , "Practicing since 3 years"
                        , "Monday, Tuesday, Friday"
                        , 1
                        , new HashSet<Specialty>() {{
                    add(new Specialty(1, 111111, "Surgeon"));
                }}
                );

        assertEquals(vet.getVetId(), 874130);
        assertEquals(vet.getFirstName(), "James");
        assertEquals(vet.getLastName(), "Carter");
        assertEquals(vet.getEmail(), "carter.james@email.com");
        assertEquals(vet.getPhoneNumber(), "2384");
        assertEquals(vet.getResume(), "Practicing since 3 years");
        assertEquals(vet.getWorkday(), "Monday, Tuesday, Friday");
        assertEquals(vet.getIsActive(), 1);
        assertEquals(vet.getSpecialties().size(), 1);
        assertEquals(vet.getSpecialties().iterator().next().getName(), "Surgeon");
    }
}
