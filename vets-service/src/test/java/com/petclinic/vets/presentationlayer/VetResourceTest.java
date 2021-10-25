package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.businesslayer.VetService;
import com.petclinic.vets.datalayer.Specialty;
import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetRepository;
import com.petclinic.vets.utils.exceptions.InvalidInputException;
import com.petclinic.vets.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Maciej Szarlinski
 * @author Christian Chitanu
 * @changes Christian Chitanu: added and modified test methods in order to fit Jcoco standards
 */

@ExtendWith(SpringExtension.class)
@WebMvcTest(VetResource.class)
@ActiveProfiles("test")
class VetResourceTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    VetService vetService;
    @MockBean
    VetRepository vetRepository;
    @Autowired
    VetResource vetResource;

    @Test
    @DisplayName("Get Vet By vetId Resource Test")
    void getVetByVetId() throws Exception {
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

        given(vetRepository.findByVetId(874130)).willReturn(Optional.of(vet));
        mvc.perform(get("/vets/" + vet.getVetId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vetId").value(874130));
    }

    @Test
    @DisplayName("Get Vet By vetId Invalid Input Resource Test")
    void getVetByVetIdInvalidInputId() throws Exception {

        assertThrows(InvalidInputException.class, () -> vetResource.findVet(0));
    }

    @Test
    @DisplayName("Get Vet By vetId Invalid Input Resource Test")
    void getVetByVetIdInvalidInput() throws Exception {
        assertThrows(NotFoundException.class, () -> {
                    vetService.getVetByVetId(-10);
                }
        );
    }

    @Test
    @DisplayName("Get List of Vets All Test")
    void shouldGetAListOfVets() throws Exception {

        Vet vet = new Vet();
        vet.setId(1);
        vet.setVetId(874130);
        vet.setFirstName("James");
        vet.setLastName("Carter");
        vet.setEmail("carter.james@email.com");
        vet.setPhoneNumber("2384");
        vet.setImage(null);
        vet.setResume("Practicing since 3 years");
        vet.setWorkday("Monday, Tuesday, Friday");
        vet.setIsActive(1);

        given(vetRepository.findAll()).willReturn(asList(vet));
        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vetId").value(874130));
    }

    @Test
    @DisplayName("Get List of Vets Resource Test")
    void shouldGetAListOfEnabledVets() throws Exception {

        Vet vet = new Vet();
        vet.setId(1);
        vet.setVetId(874130);
        vet.setFirstName("James");
        vet.setLastName("Carter");
        vet.setEmail("carter.james@email.com");
        vet.setPhoneNumber("2384");
        vet.setImage(null);
        vet.setResume("Practicing since 3 years");
        vet.setWorkday("Monday, Tuesday, Friday");
        vet.setIsActive(1);

        given(vetRepository.findAllEnabledVets()).willReturn(asList(vet));
        mvc.perform(get("/vets/enabled").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vetId").value(874130));
    }

    @Test
    @DisplayName("Get List of Disabled Vets Resource Test")
    void shouldGetAListOfDisabledVets() throws Exception {

        Vet vet = new Vet();
        vet.setId(1);
        vet.setVetId(874130);
        vet.setFirstName("James");
        vet.setLastName("Carter");
        vet.setEmail("carter.james@email.com");
        vet.setPhoneNumber("2384");
        vet.setImage(null);
        vet.setResume("Practicing since 3 years");
        vet.setWorkday("Monday, Tuesday, Friday");
        vet.setIsActive(0);

        given(vetRepository.findAllDisabledVets()).willReturn(asList(vet));
        mvc.perform(get("/vets/disabled").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vetId").value(874130));
    }

    @Test
    @DisplayName("Create Vet Resource Test")
    void createVet() throws Exception {
        //assert
        Specialty specialty = new Specialty(1, 123456, "tester");
        Set<Specialty> specialties = new HashSet<>();
        specialties.add(specialty);
        Vet vet2 = new Vet();
        vet2.setId(1);
        vet2.setVetId(874130);
        vet2.setFirstName("James");
        vet2.setLastName("Carter");
        vet2.setEmail("carter.james@email.com");
        vet2.setPhoneNumber("2384");
        vet2.setResume("Practicing since 3 years");
        vet2.setWorkday("Monday, Tuesday, Friday");
        vet2.setIsActive(1);
        vet2.setSpecialties(specialties);
        when(vetRepository.save(any(Vet.class))).thenReturn(vet2);
        mvc.perform(post("/vets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\": \"James\"," +
                                "\"lastName\": \"Carter\"," +
                                "\"email\": \"carter.james@email.com\"," +
                                "\"phoneNumber\": 2384," +
                                "\"resume\": \"Practicing since 3 years\"," +
                                "\"workday\": \"Monday, Tuesday, Friday\"," +
                                "\"specialties\":[{\"id\":2, \"specialtyId\":234567, \"name\":\"tester\"}]," +
                                "\"isActive\": 1}"))

                // Validate the response code and content type
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.vetId").exists());
    }

    @Test
    @DisplayName("Update Vet Resource Test")
    void updateVet() throws Exception {
        //arrange
        byte[] image = {0x00, 0x00, 0x00, 0x00, 0x00, 0x60, 0x06, 0x00, 0x01, (byte) 0xc1, (byte) 0x83, (byte) 0x80, 0x07, (byte) 0xc3, (byte) 0xc3, (byte) 0xe0,
                0x0f, (byte) 0xe3, (byte) 0xc7, (byte) 0xf0, 0x0f, (byte) 0xff, (byte) 0xff, (byte) 0xf0, 0x1f, (byte) 0xff, (byte) 0xff, (byte) 0xf8, 0x1f, (byte) 0xff, (byte) 0xff, (byte) 0xf8,
                0x0f, (byte) 0xff, (byte) 0xff, (byte) 0xf0, 0x0f, 0x3b, (byte) 0xdc, (byte) 0xf0, 0x06, 0x01, (byte) 0x80, 0x60, 0x03, 0x01, (byte) 0x80, (byte) 0xc0,
                0x00, 0x00, 0x00, 0x00};
        Specialty specialty = new Specialty(1, 123456, "tester");
        Set<Specialty> specialties = new HashSet<>();
        specialties.add(specialty);
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
        vet.setSpecialties(specialties);
        vet.setImage(image);
        //act
        when(vetRepository.findByVetId(anyInt())).thenReturn(Optional.of(vet));
        when(vetRepository.save(any())).thenAnswer(i -> i.getArgument(0, Vet.class));
        //assert


        mvc.perform(put("/vets/{vetId}", vet.getVetId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vetId\": 874130," +
                                "\"firstName\": \"Jamess\"," +
                                "\"lastName\": \"Carterr\"," +
                                "\"email\": \"carter.james2@email.com\"," +
                                "\"phoneNumber\": 2383," +
                                "\"resume\": \"Practicing since 4 years\"," +
                                "\"workday\": \"Monday, Friday\"," +
                                "\"image\": \"NULL\"," +
                                "\"specialties\":[{\"id\":1, \"specialtyId\":234567, \"name\":\"testerA\"}]," +
                                "\"isActive\": 1}"))

                // Validate the response code and content type
                .andExpect(status().isNoContent())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.vetId").value(874130))
                .andExpect(jsonPath("$.firstName").value("Jamess"))
                .andExpect(jsonPath("$.lastName").value("Carterr"))
                .andExpect(jsonPath("$.email").value("carter.james2@email.com"))
                .andExpect(jsonPath("$.phoneNumber").value("(514)-634-8276 #2383"))
                .andExpect(jsonPath("$.resume").value("Practicing since 4 years"))
                .andExpect(jsonPath("$.workday").value("Monday, Friday"))
                .andExpect(jsonPath("$.image").value("NULL"))
                .andExpect(jsonPath("$.specialties[0].name").value("testerA"))
                .andExpect(jsonPath("$.isActive").value(1));
    }


    @Test
    @DisplayName("Disable Vet Resource Test")
    void disableAVet() throws Exception {

        Vet vet = new Vet();
        vet.setId(1);
        vet.setVetId(874130);
        vet.setFirstName("James");
        vet.setLastName("Carter");
        vet.setEmail("carter.james@email.com");
        vet.setPhoneNumber("2384");
        vet.setResume("Practicing since 3 years");
        vet.setWorkday("Monday, Tuesday, Friday");
        vet.setIsActive(0);

        given(vetRepository.findByVetId(vet.getVetId())).willReturn(Optional.of(vet));
        mvc.perform(put("/vets/{vetId}/disableVet", vet.getVetId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"vetId\": 874130," +
                                "\"firstName\": \"James\"," +
                                "\"lastName\": \"Carter\"," +
                                "\"email\": \"carter.james@email.com\"," +
                                "\"phoneNumber\": 2384," +
                                "\"resume\": \"Practicing since 3 years\"," +
                                "\"workday\": \"Monday, Tuesday, Friday\"," +
                                "\"isActive\": 0}"))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.vetId").value(874130))
                .andExpect(jsonPath("$.firstName").value("James"))
                .andExpect(jsonPath("$.lastName").value("Carter"))
                .andExpect(jsonPath("$.email").value("carter.james@email.com"))
                .andExpect(jsonPath("$.phoneNumber").value("(514)-634-8276 #2384"))
                .andExpect(jsonPath("$.resume").value("Practicing since 3 years"))
                .andExpect(jsonPath("$.workday").value("Monday, Tuesday, Friday"))
                .andExpect(jsonPath("$.isActive").value(0));
    }

    @Test
    @DisplayName("Enable Vet Resource Test")
    void enableAVet() throws Exception {
        Vet vet = new Vet();
        vet.setId(1);
        vet.setVetId(874130);
        vet.setFirstName("James");
        vet.setLastName("Carter");
        vet.setEmail("carter.james@email.com");
        vet.setPhoneNumber("2384");
        vet.setResume("Practicing since 3 years");
        vet.setWorkday("Monday, Tuesday, Friday");
        vet.setIsActive(0);
        given(vetRepository.findByVetId(vet.getVetId())).willReturn(Optional.of(vet));
        mvc.perform(put("/vets/{vetId}/enableVet", vet.getVetId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"vetId\": 874130," +
                                "\"firstName\": \"James\"," +
                                "\"lastName\": \"Carter\"," +
                                "\"email\": \"carter.james@email.com\"," +
                                "\"phoneNumber\": 2384," +
                                "\"resume\": \"Practicing since 3 years\"," +
                                "\"workday\": \"Monday, Tuesday, Friday\"," +
                                "\"isActive\": 1}"))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.vetId").value(874130))
                .andExpect(jsonPath("$.firstName").value("James"))
                .andExpect(jsonPath("$.lastName").value("Carter"))
                .andExpect(jsonPath("$.email").value("carter.james@email.com"))
                .andExpect(jsonPath("$.phoneNumber").value("(514)-634-8276 #2384"))
                .andExpect(jsonPath("$.resume").value("Practicing since 3 years"))
                .andExpect(jsonPath("$.workday").value("Monday, Tuesday, Friday"))
                .andExpect(jsonPath("$.isActive").value(1));
    }

    @Test
    @DisplayName("Get All Fields Vet Resource Test")
    void shouldGetAllTheFieldsForAVet() throws Exception {

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

        given(vetRepository.findAllEnabledVets()).willReturn(asList(vet));
        mvc.perform(get("/vets/enabled").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vetId").value(874130))
                .andExpect(jsonPath("$[0].firstName").value("James"))
                .andExpect(jsonPath("$[0].lastName").value("Carter"))
                .andExpect(jsonPath("$[0].email").value("carter.james@email.com"))
                .andExpect(jsonPath("$[0].phoneNumber").value("(514)-634-8276 #2384"))
                .andExpect(jsonPath("$[0].resume").value("Practicing since 3 years"))
                .andExpect(jsonPath("$[0].workday").value("Monday, Tuesday, Friday"))
                .andExpect(jsonPath("$[0].isActive").value(1));
    }


    @Test
    @DisplayName("Vet input submission cleanup and validation Resource Test")
    void VetRegisterDataFilterAndValidationTest() throws Exception {

        Vet vet = new Vet();
        vet.setId(1);
        vet.setVetId(87413012);
        vet.setFirstName(" James215 ");
        vet.setLastName(" 23Carter32 ");
        vet.setEmail(" car ter . ja mes @ ema il . co m ");
        vet.setPhoneNumber(" #2384 fwfkdsbajnl####");
        vet.setResume("Practicing since 3 years");
        vet.setWorkday("Monday,Tuesday,         Friday");
        vet.setIsActive(1);

        given(vetRepository.findAllEnabledVets()).willReturn(asList(vet));

        mvc.perform(get("/vets/enabled").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vetId").value(874130))
                .andExpect(jsonPath("$[0].firstName").value("James"))
                .andExpect(jsonPath("$[0].lastName").value("Carter"))
                .andExpect(jsonPath("$[0].email").value("carter.james@email.com"))
                .andExpect(jsonPath("$[0].phoneNumber").value("(514)-634-8276 #2384"))
                .andExpect(jsonPath("$[0].resume").value("Practicing since 3 years"))
                .andExpect(jsonPath("$[0].workday").value("Monday, Tuesday, Friday"))
                .andExpect(jsonPath("$[0].isActive").value(1));
    }

    @Test
    @DisplayName("Delete Vet Test Valid VetId Routing and ui response")
    void deleteVetValidVetIdRoutingAndUiResponse1() throws Exception {

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

        vetRepository.deleteByVetId(vet.getVetId());
        mvc.perform(get("/vets/details/874130"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete Vet Test Valid VetId")
    void deleteVetValidVetIdShouldDeleteVetFromRepo() throws Exception {

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

        given(vetRepository.findByVetId(vet.getVetId())).willReturn(Optional.of(vet));

        mvc.perform(delete("/vets/" + vet.getVetId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }
}
