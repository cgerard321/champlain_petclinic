package com.petclinic.visits.presentationlayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.visits.businesslayer.VisitsService;
import com.petclinic.visits.datalayer.Visit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.ArrayList;
import java.util.Date;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static com.petclinic.visits.datalayer.Visit.visit;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VisitResource.class)
@ActiveProfiles("test")
public class VisitResourceTest {


	@Autowired
	MockMvc mvc;

	@MockBean
	VisitsService visitsService;


	@Autowired
	ObjectMapper objectMapper;

	@Test
	void whenValidPetIdThenShouldReturnVisitsForPet() throws Exception {

		given(visitsService.getVisitsForPet(1))
				.willReturn(
						asList(
								visit()
										.id(1)
										.petId(1)
										.build(),
								visit()
										.id(2)
										.petId(1)
										.build()
						)
				);

		mvc.perform(get("/visits/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[1].id").value(2))
				.andExpect(jsonPath("$[0].petId").value(1))
				.andExpect(jsonPath("$[1].petId").value(1));
	}


	@Test
	void shouldFetchVisits() throws Exception {
		given(visitsService.getVisitsForPets(asList(111, 222)))
				.willReturn(
						asList(
								visit()
										.id(1)
										.petId(111)
										.build(),
								visit()
										.id(2)
										.petId(222)
										.build(),
								visit()
										.id(3)
										.petId(222)
										.build()
						)
				);

		mvc.perform(get("/pets/visits?petId=111,222"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].id").value(1))
				.andExpect(jsonPath("$.items[1].id").value(2))
				.andExpect(jsonPath("$.items[2].id").value(3))
				.andExpect(jsonPath("$.items[0].petId").value(111))
				.andExpect(jsonPath("$.items[1].petId").value(222))
				.andExpect(jsonPath("$.items[2].petId").value(222));
	}

	@Test
	void shouldUpdateVisit() throws Exception{
		when(visitsService.updateVisit(any(Visit.class)))
				.thenReturn(new Visit(1, new Date(), "Desc-1", 1));

		mvc.perform(put("/owners/*/pets/{petId}/visits/{id}", 1, 1)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content("{\"date\": \"2011-03-04\", \"description\": \"Desc-1 Updated\"}")
				.characterEncoding("utf-8"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.petId").value(1));

	}

	@Test
	void updateVisitInvalidParameterStringPetId() throws Exception{
		when(visitsService.updateVisit(any(Visit.class)))
				.thenReturn(new Visit(1, new Date(), "Desc-1", 1));

		mvc.perform(put("/owners/*/pets/{petId}/visits/{id}", "invalid_pet_id",1)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content("{\"date\": \"2011-03-04\", \"description\": \"Desc-1 Updated\"}")
				.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void updateVisitInvalidParameterStringVisitId() throws Exception{
		when(visitsService.updateVisit(any(Visit.class)))
				.thenReturn(new Visit(1, new Date(), "Desc-1", 1));

		mvc.perform(put("/owners/*/pets/{petId}/visits/{id}", 1, "invalid_visit_id")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content("{\"date\": \"2011-03-04\", \"description\": \"Desc-1 Updated\"}")
				.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void whenValidVisitIdDeleteTheVisit() throws Exception {
		mvc.perform(delete("/visits/1"))
				.andExpect(status().isOk());
		verify(visitsService, times(1)).deleteVisit(1);
	}

	@Test
	public void whenInvalidVisitIdDontDeleteTheVisit() throws Exception {
		mvc.perform(delete("/visits/faso"))
				.andExpect(status().isBadRequest());
	}



	@Test
	void shouldCreateConfirmedVisit() throws Exception {
		Visit visit = visit().id(1).petId(111).status(true).build();

		given(visitsService.addVisit(visit)).willReturn(visit);

		mvc.perform(post("/owners/*/pets/{petId}/visits", 1).content("{\"id\": 1, \"date\": \"2011-03-04\", \"description\": \"Desc-1\", \"petId\": 1, \"status\": \"true\"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
	}

	@Test
	void shouldCreateCanceledVisit() throws Exception {
		Visit visit = visit().id(1).petId(111).status(false).build();

		given(visitsService.addVisit(visit)).willReturn(visit);

		mvc.perform(post("/owners/*/pets/{petId}/visits", 1).content("{\"id\": 1, \"date\": \"2011-03-04\", \"description\": \"Desc-1\", \"petId\": 1, \"status\": \"false\"}")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
	}


	@Test
	void shouldCreateVisit() throws Exception {
		Visit expectedVisit = visit().id(1).petId(1).date(new Date()).description("CREATED VISIT").practitionerId(123456).build();

		when(visitsService.addVisit(any())).thenReturn(expectedVisit);

		mvc.perform(post("/owners/*/pets/{petId}/visits", 1)
				.content(objectMapper.writeValueAsString(expectedVisit))
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.description").value(expectedVisit.getDescription()));
	}

	@Test
	void shouldFailToCreateVisitBadRequest() throws Exception {
		Visit expectedVisit = visit().petId(1).date(new Date()).description("CREATED VISIT").practitionerId(123456).build();

		when(visitsService.addVisit(any())).thenReturn(expectedVisit);

		mvc.perform(post("/owners/*/pets/{petId}/visits", 1)
				.content("")
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}


	@Test
	void whenInvalidPetIdThenShouldReturnBadRequest() throws Exception {
		mvc.perform(get("/visits/FADAW"))
				.andExpect(status().isBadRequest());
	}

}


