package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.datalayer.Vet;
import com.petclinic.vets.datalayer.VetRepository;
import com.petclinic.vets.presentationlayer.VetResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.swing.text.html.parser.Entity;
import java.util.Optional;
import static  org.hamcrest.MatcherAssert.assertThat;
import static  org.hamcrest.Matcher.*;
import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Maciej Szarlinski
 */

@ExtendWith(SpringExtension.class)
@WebMvcTest(VetResource.class)
@ActiveProfiles("test")
class VetResourceTest {

	@Autowired
	MockMvc mvc;

	@MockBean
	VetRepository vetRepository;


	@Test
	@DisplayName("Should get a list of vets")
	void shouldGetAListOfVets() throws Exception {

		Vet vet = new Vet();
		vet.setId(1);

		given(vetRepository.findAll()).willReturn(asList(vet));

		mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1));
	}

	@Test
	void disableAVet() throws Exception {
		//arrange
		Vet vet = new Vet();
		vet.setIsActive(0);
		//act
		given(vetRepository.findAll()).willReturn(asList(vet));
		//assert
		mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].isActive").value(0));
	}


	@Test
	@DisplayName("Should get all the fields for a vet and check if they are okay")
	void shouldGetAllTheFieldsForAVet() throws Exception{

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

		given(vetRepository.findAll()).willReturn(asList(vet));

		mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1))
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
	@DisplayName("Vet input submission cleanup and validation Test")
	void VetRegisterDataFilterAndValidationTest() throws Exception{

		Vet vet = new Vet();
		vet.setId(1);
		vet.setVetId(87413012);
		vet.setFirstName(" James215 ");
		vet.setLastName(" 23Carter32 ");
		vet.setEmail(" car ter . ja mes @ ema il . co m ");
		vet.setPhoneNumber(" #2384 fwfkdsbajnl####");
		vet.setResume("Practicing since 3 years");
		vet.setWorkday("Monday,Tuesday,         Friday");
		vet.setIsActive(5);

		given(vetRepository.findAll()).willReturn(asList(vet));

		mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1))
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
	void addANewVet() throws Exception {
		//arrange
		Vet vet = new Vet();
		vet.setId(1);
		//act //assert

		given(vetRepository.findAll()).willReturn(asList(vet));
		assertEquals(vet.getId(), 1);
	}
}
