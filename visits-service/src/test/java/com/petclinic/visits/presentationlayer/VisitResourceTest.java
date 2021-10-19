package com.petclinic.visits.presentationlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.visits.businesslayer.VisitsService;
import com.petclinic.visits.datalayer.Visit;
import com.petclinic.visits.datalayer.VisitDTO;
import com.petclinic.visits.utils.exceptions.InvalidInputException;
import com.petclinic.visits.utils.exceptions.NotFoundException;
import com.petclinic.visits.utils.http.ControllerExceptionHandler;
import com.petclinic.visits.utils.http.HttpErrorInfo;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
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

/*
 * This class tests the methods of the VisitResource class, as well as the methods in the utils package.
 *
 * Contributors:
 *   70963776+cjayneb@users.noreply.github.com
 */

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


	// REST CONTROLLER TESTING
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
	public void whenInvalidVisitIdDontDeleteAndReturnBadRequest() throws Exception {
		mvc.perform(delete("/visits/faso"))
				.andExpect(status().isBadRequest());
		verify(visitsService, times(0)).deleteVisit(anyInt());
	}



	@Test
	void shouldCreateConfirmedVisit() throws Exception {
		VisitDTO visitDTO = new VisitDTO();
		visitDTO.setStatus(true);

		given(visitsService.addVisit(any())).willReturn(visitDTO);

		mvc.perform(post("/owners/*/pets/{petId}/visits", 1).content(objectMapper.writeValueAsString(visitDTO))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
	}

	@Test
	void shouldCreateCanceledVisit() throws Exception {
		VisitDTO visitDTO = new VisitDTO();
		visitDTO.setStatus(false);

		given(visitsService.addVisit(any())).willReturn(visitDTO);

		mvc.perform(post("/owners/*/pets/{petId}/visits", 1).content(objectMapper.writeValueAsString(visitDTO))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
	}


	@Test
	void shouldCreateVisit() throws Exception {
		VisitDTO visitDTO = new VisitDTO();
		visitDTO.setVisitId("9161747b-886e-4d7c-9616-188de90c1306");
		visitDTO.setDescription("Description");

		when(visitsService.addVisit(any())).thenReturn(visitDTO);

		mvc.perform(post("/owners/*/pets/{petId}/visits", 1)
				.content(objectMapper.writeValueAsString(visitDTO))
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.description").value(visitDTO.getDescription()));
	}

	@Test
	void shouldFailToCreateVisitBadRequest() throws Exception {
		VisitDTO visitDTO = new VisitDTO();
		visitDTO.setVisitId("9161747b-886e-4d7c-9616-188de90c1306");
		visitDTO.setDescription("Description");

		when(visitsService.addVisit(any())).thenReturn(visitDTO);

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

	@Test
	void whenEmptyDescriptionThenShouldHandleInvalidInputException() throws Exception{
		Visit expectedVisit = visit().id(1).petId(1).date(new Date()).description("").practitionerId(123456).build();

		when(visitsService.addVisit(any())).thenThrow(new InvalidInputException("Visit description required."));

		mvc.perform(post("/owners/1/pets/{petId}/visits", 1)
				.content(objectMapper.writeValueAsString(expectedVisit))
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidInputException))
				.andExpect(result -> assertEquals("Visit description required.", result.getResolvedException().getMessage()));


	}

	@Test
	void whenPetDoesNotExistThenShouldHandleNotFoundException() throws Exception {
		Visit expectedVisit = visit().id(1).petId(65).date(new Date()).description("description").practitionerId(123456).build();

		when(visitsService.addVisit(any())).thenThrow(new NotFoundException("Pet does not exist."));

		mvc.perform(post("/owners/1/pets/{petId}/visits", 65)
				.content(objectMapper.writeValueAsString(expectedVisit))
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
				.andExpect(result -> assertEquals("Pet does not exist.", result.getResolvedException().getMessage()));
	}

	@Test
	void shouldReturnPreviousVisits() throws Exception {
		Date beforeNow = new Date(System.currentTimeMillis() - 100000);
		List<Visit> previousVisits = asList(
				visit()
						.id(1)
						.petId(1)
						.date(beforeNow)
						.build());

		when(visitsService.getVisitsForPet(1, false)).thenReturn(previousVisits);

		mvc.perform(get("/visits/previous/{petId}", 1))
				.andExpect(status().isOk());

		verify(visitsService, times(1)).getVisitsForPet(1, false);
	}

	@Test
	void shouldReturnScheduledVisits() throws Exception {
		Date afterNow = new Date(System.currentTimeMillis() + 100000);
		List<Visit> scheduledVisits = asList(
				visit()
						.id(1)
						.petId(1)
						.date(afterNow)
						.build());

		when(visitsService.getVisitsForPet(1, true)).thenReturn(scheduledVisits);

		mvc.perform(get("/visits/scheduled/{petId}", 1))
				.andExpect(status().isOk());
		verify(visitsService, times(1)).getVisitsForPet(1, true);

	}

	@Test
	void whenFetchingWithNegativePetIdShouldHandleInvalidInputException() throws Exception {
		when(visitsService.getVisitsForPet(-1)).thenThrow(new InvalidInputException("PetId can't be negative."));

		mvc.perform(get("/visits/{petId}", -1))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidInputException))
				.andExpect(result -> assertEquals("PetId can't be negative.", result.getResolvedException().getMessage()));
	}

	@Test
	void whenFetchingStringDatesWithNegativePractitionerIdThenShouldHandleInvalidInputException() throws Exception {
		when(visitsService.getVisitsForPractitioner(-1)).thenThrow(new InvalidInputException("PractitionerId can't be negative."));

		mvc.perform(get("/visits/vets/{practitionerId}",-1))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidInputException))
				.andExpect(result -> assertEquals("PractitionerId can't be negative.", result.getResolvedException().getMessage()));
	}

	@Test
	void whenFetchingVisitsWithValidPractitionerIdThenShouldReturnListOfVisits() throws Exception {
		List<Visit> returnedVisits =asList(
				visit()
						.id(1)
						.petId(1)
						.practitionerId(200200)
						.build(),
				visit()
						.id(2)
						.petId(1)
						.practitionerId(200200)
						.build());

		given(visitsService.getVisitsForPractitioner(200200)).willReturn(returnedVisits);

		mvc.perform(get("/visits/vets/{practitionerId}",200200))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].practitionerId").value(200200))
				.andExpect(jsonPath("$[1].practitionerId").value(200200));
	}

	@Test
	void shouldReturnAllVisitsForSpecifiedPractitionerIdAndMonth() throws Exception {
		Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-01");
		Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-31");

		given(visitsService.getVisitsByPractitionerIdAndMonth(1, startDate, endDate))
				.willReturn(
						Collections.singletonList(
								visit()
										.id(1)
										.petId(1)
										.date(new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-15"))
										.practitionerId(1)
										.build())
				);

		mvc.perform(get("/visits/calendar/{practitionnerId}?dates={startDate},{endDate}", 1, "2021-10-01", "2021-10-31"))
				.andExpect(status().isOk());

		verify(visitsService, times(1)).getVisitsByPractitionerIdAndMonth(1, startDate, endDate);
	}

	@Test
	void whenFetchingWithNegativePractitionerIdShouldHandleInvalidInputException() throws Exception {
		Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-01");
		Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-31");

		ArrayList<Date> dates = new ArrayList<Date>();
		dates.add(startDate);
		dates.add(endDate);

		when(visitsService.getVisitsByPractitionerIdAndMonth(-1, startDate, endDate)).thenThrow(new InvalidInputException("PractitionerId can't be negative."));

		mvc.perform(get("/visits/calendar/{practitionnerId}?dates={startDate},{endDate}", -1, "2021-10-01", "2021-10-31"))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidInputException))
				.andExpect(result -> assertEquals("PractitionerId can't be negative.", Objects.requireNonNull(result.getResolvedException()).getMessage()));
	}


}


