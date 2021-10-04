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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Optional;

import static  org.hamcrest.MatcherAssert.assertThat;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

	@Test
	@DisplayName("Get Vet By vetId Resource Test")
	void getVetByVetId() throws Exception{
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
		mvc.perform(get("/vets/"+vet.getVetId()).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.vetId").value(874130));
	}
	@Test
	@DisplayName("Get Vet By vetId Invalid Input Ressource Test")
	void getVetByVetIdInvalidInput() throws Exception{
			assertThrows(NotFoundException.class,()->{
				vetService.getVetByVetId(-10);
			}
		);
	}

	@Test
	@DisplayName("Get List of Vets Resource Test")
	void shouldGetAListOfVets() throws Exception {

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
		mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
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
		when(vetRepository.save(any(Vet.class))).thenReturn(vet2);
		mvc.perform(post("/vets")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"firstName\": \"James\"," +
								"\"lastName\": \"Carter\"," +
								"\"email\": \"carter.james@email.com\"," +
								"\"phoneNumber\": 2384," +
								"\"resume\": \"Practicing since 3 years\"," +
								"\"workday\": \"Monday, Tuesday, Friday\"," +
								"\"isActive\": 1}"))

				// Validate the response code and content type
				.andExpect(status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.vetId").exists());
	}

	@Test
	@DisplayName("Update Vet Resource Test")
	void updateVet() throws Exception {
		//arrange
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
		//act
		given(vetRepository.findByVetId(vet.getVetId())).willReturn(Optional.of(vet));
		//assert
		mvc.perform(put("/vets/{vetId}",vet.getVetId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{ \"id\": 1," +
								"\"vetId\": 874130," +
								"\"firstName\": \"Jamess\"," +
								"\"lastName\": \"Carterr\"," +
								"\"email\": \"carter.james2@email.com\"," +
								"\"phoneNumber\": 2383," +
								"\"resume\": \"Practicing since 4 years\"," +
								"\"workday\": \"Monday, Friday\"," +
								"\"isActive\": 1}"))

				// Validate the response code and content type
				.andExpect(status().isNoContent())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.vetId").value(874130))
				.andExpect(jsonPath("$.firstName").value("Jamess"))
				.andExpect(jsonPath("$.lastName").value("Carterr"))
				.andExpect(jsonPath("$.email").value("carter.james2@email.com"))
				.andExpect(jsonPath("$.phoneNumber").value("(514)-634-8276 #2383"))
				.andExpect(jsonPath("$.resume").value("Practicing since 4 years"))
				.andExpect(jsonPath("$.workday").value("Monday, Friday"))
				.andExpect(jsonPath("$.isActive").value(1));;
	}

	@Test
	@DisplayName("Disable Vet Resource Test")
	void disableAVet() throws Exception {
		//arrange
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
		//act
		given(vetRepository.findByVetId(vet.getVetId())).willReturn(Optional.of(vet));
		//assert
		mvc.perform(put("/vets/{vetId}/disableVet",vet.getVetId())
								.contentType(MediaType.APPLICATION_JSON)
								.content("{ \"id\": 1," +
										"\"vetId\": 874130," +
										"\"firstName\": \"James\"," +
										"\"lastName\": \"Carter\"," +
										"\"email\": \"carter.james@email.com\"," +
										"\"phoneNumber\": 2384," +
										"\"resume\": \"Practicing since 3 years\"," +
										"\"workday\": \"Monday, Tuesday, Friday\"," +
										"\"isActive\": 0}"))

						// Validate the response code and content type
						.andExpect(status().isOk())
						.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.vetId").value(874130))
				.andExpect(jsonPath("$.firstName").value("James"))
				.andExpect(jsonPath("$.lastName").value("Carter"))
				.andExpect(jsonPath("$.email").value("carter.james@email.com"))
				.andExpect(jsonPath("$.phoneNumber").value("(514)-634-8276 #2384"))
				.andExpect(jsonPath("$.resume").value("Practicing since 3 years"))
				.andExpect(jsonPath("$.workday").value("Monday, Tuesday, Friday"))
				.andExpect(jsonPath("$.isActive").value(0));;
	}

	@Test
	@DisplayName("Enable Vet Resource Test")
	void enableAVet() throws Exception {
		//arrange
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
		//act
		given(vetRepository.findByVetId(vet.getVetId())).willReturn(Optional.of(vet));
		//assert
		mvc.perform(put("/vets/{vetId}/enableVet",vet.getVetId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{ \"id\": 1," +
								"\"vetId\": 874130," +
								"\"firstName\": \"James\"," +
								"\"lastName\": \"Carter\"," +
								"\"email\": \"carter.james@email.com\"," +
								"\"phoneNumber\": 2384," +
								"\"resume\": \"Practicing since 3 years\"," +
								"\"workday\": \"Monday, Tuesday, Friday\"," +
								"\"isActive\": 1}"))

				// Validate the response code and content type
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.vetId").value(874130))
				.andExpect(jsonPath("$.firstName").value("James"))
				.andExpect(jsonPath("$.lastName").value("Carter"))
				.andExpect(jsonPath("$.email").value("carter.james@email.com"))
				.andExpect(jsonPath("$.phoneNumber").value("(514)-634-8276 #2384"))
				.andExpect(jsonPath("$.resume").value("Practicing since 3 years"))
				.andExpect(jsonPath("$.workday").value("Monday, Tuesday, Friday"))
				.andExpect(jsonPath("$.isActive").value(1));;
	}

	@Test
	@DisplayName("Get All Fields Vet Resource Test")
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

		given(vetRepository.findAllEnabledVets()).willReturn(asList(vet));
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
	@DisplayName("Vet input submission cleanup and validation Resource Test")
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
		vet.setIsActive(1);

		given(vetRepository.findAllEnabledVets()).willReturn(asList(vet));

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

}
