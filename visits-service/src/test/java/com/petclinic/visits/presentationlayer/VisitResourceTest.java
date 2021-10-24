package com.petclinic.visits.presentationlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.visits.businesslayer.VisitsService;
import com.petclinic.visits.datalayer.Visit;
import com.petclinic.visits.datalayer.VisitDTO;
import com.petclinic.visits.datalayer.VisitIdLessDTO;
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

import java.text.ParseException;
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

	List<VisitDTO> visitDTOList = Arrays.asList(
			new VisitDTO(UUID.randomUUID().toString(), new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-01"),
					"Description", 200, 123456, true),
			new VisitDTO(UUID.randomUUID().toString(), new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-01"),
					"Description", 200, 123456, true),
			new VisitDTO(UUID.randomUUID().toString(), new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-01"),
					"Description", 200, 123456, true)
	);

	VisitIdLessDTO visitIdLessDTO = new VisitIdLessDTO(new Date(System.currentTimeMillis()), "Description", 200, 12345, true);
	VisitDTO visitDTO = new VisitDTO(UUID.randomUUID().toString(), new Date(System.currentTimeMillis()), "Description", 200, 123456, true);

	public VisitResourceTest() throws ParseException {
	}

	// TESTS FOR FETCHING A SINGLE VISIT ----------------------------------------------------------------------
	@Test
	void shouldReturnVisitWhenFetchingVisitWithValidVisitId() throws Exception {
		given(visitsService.getVisitByVisitId(anyString()))
				.willReturn(visitDTO);

		mvc.perform(get("/visit/{visitId}", visitDTO.getVisitId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.visitId").value(visitDTO.getVisitId()))
				.andExpect(jsonPath("$.petId").value(visitDTO.getPetId()))
				.andExpect(jsonPath("$.description").value(visitDTO.getDescription()))
				.andExpect(jsonPath("$.practitionerId").value(visitDTO.getPractitionerId()))
				.andExpect(jsonPath("$.status").value(visitDTO.isStatus()))
				.andExpect(jsonPath("$.date").exists());
	}

	@Test
	void shouldHandleInvalidInputExceptionWhenFetchingVisitWithInvalidVisitId() throws Exception {
		when(visitsService.getVisitByVisitId(anyString())).thenThrow(new InvalidInputException("VisitId not in the right format."));

		mvc.perform(get("/visit/{visitId}", "invalid"))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidInputException))
				.andExpect(result -> assertEquals("VisitId not in the right format.", result.getResolvedException().getMessage()));
	}

	@Test
	void shouldHandleNotFoundExceptionWhenFetchingVisitWithNonExistentVisitId() throws Exception {
		String randomId = UUID.randomUUID().toString();
		String expectedExMsg = "Visit with visitId: " + randomId + " does not exist.";
		when(visitsService.getVisitByVisitId(anyString())).thenThrow(new NotFoundException(expectedExMsg));

		mvc.perform(get("/visit/{visitId}", randomId))
				.andExpect(status().isNotFound())
				.andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
				.andExpect(result -> assertEquals(expectedExMsg, result.getResolvedException().getMessage()));
	}

	// TESTS FOR UPDATING A VISIT ----------------------------------------------------------------------
	@Test
	void shouldUpdateVisitWhenValidRequest() throws Exception{
		when(visitsService.updateVisit(any(VisitDTO.class)))
				.thenReturn(visitDTO);

		mvc.perform(put("/owners/*/pets/{petId}/visits/{visitId}", 200, visitDTO.getVisitId())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(visitDTO))
				.characterEncoding("utf-8"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.visitId").value(visitDTO.getVisitId()))
				.andExpect(jsonPath("$.date").exists())
				.andExpect(jsonPath("$.description").value(visitDTO.getDescription()))
				.andExpect(jsonPath("$.petId").value(visitDTO.getPetId()))
				.andExpect(jsonPath("$.practitionerId").value(visitDTO.getPractitionerId()));
	}

	@Test
	void shouldReturnBadRequestWhenInvalidParameterStringPetId() throws Exception{
		when(visitsService.updateVisit(any(VisitDTO.class)))
				.thenReturn(visitDTO);

		mvc.perform(put("/owners/*/pets/{petId}/visits/{id}", "invalid_pet_id",visitDTO.getVisitId())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(visitDTO))
				.characterEncoding("utf-8"))
				.andExpect(status().isBadRequest());
	}

	// TESTS FOR DELETING A VISIT ----------------------------------------------------------------------
	@Test
	public void shouldCallServiceDeleteVisitWhenDeletingWithValidVisitId() throws Exception {
		String visitId = UUID.randomUUID().toString();
		mvc.perform(delete("/visits/{visitId}", visitId))
				.andExpect(status().isOk());
		verify(visitsService, times(1)).deleteVisit(visitId);
	}

	// TESTS FOR CREATING A VISIT ----------------------------------------------------------------------
	@Test
	void shouldReturnCreatedVisitWhenValidRequest() throws Exception {
		given(visitsService.addVisit(any())).willReturn(visitDTO);

		mvc.perform(post("/owners/*/pets/{petId}/visits", 200)
				.content(objectMapper.writeValueAsString(visitIdLessDTO))
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.visitId").value(visitDTO.getVisitId()))
				.andExpect(jsonPath("$.date").exists())
				.andExpect(jsonPath("$.description").value(visitDTO.getDescription()))
				.andExpect(jsonPath("$.petId").value(visitDTO.getPetId()))
				.andExpect(jsonPath("$.practitionerId").value(visitDTO.getPractitionerId()));
	}

	@Test
	void shouldReturnBadRequestWhenCreatingVisitWithInvalidBody() throws Exception {
		mvc.perform(post("/owners/*/pets/{petId}/visits", 200)
				.content("")
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturnBadRequestWhenCreatingVisitWithInvalidPetId() throws Exception {
		mvc.perform(post("/owners/*/pets/{petId}/visits", "invalid")
				.content(objectMapper.writeValueAsString(visitIdLessDTO))
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldHandleInvalidInputExceptionWhenThrownInServiceLayer() throws Exception{
		when(visitsService.addVisit(any())).thenThrow(new InvalidInputException("Visit description required."));

		mvc.perform(post("/owners/1/pets/{petId}/visits", 200)
				.content(objectMapper.writeValueAsString(visitIdLessDTO))
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidInputException))
				.andExpect(result -> assertEquals("Visit description required.", result.getResolvedException().getMessage()));
	}

	@Test
	void shouldHandleNotFoundExceptionWhenThrownInServiceLayer() throws Exception {
		when(visitsService.addVisit(any())).thenThrow(new NotFoundException("Pet does not exist."));

		mvc.perform(post("/owners/1/pets/{petId}/visits", 404)
				.content(objectMapper.writeValueAsString(visitIdLessDTO))
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding("utf-8")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
				.andExpect(result -> assertEquals("Pet does not exist.", result.getResolvedException().getMessage()));
	}

	// TESTS FOR PREVIOUS AND SCHEDULED VISITS ----------------------------------------------------------------------
	@Test
	void shouldReturnListOfVisitsWhenFetchingPreviousVisitsWithValidPetId() throws Exception {
		when(visitsService.getVisitsForPet(200, false)).thenReturn(visitDTOList);

		mvc.perform(get("/visits/previous/{petId}", 200))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[0].visitId").value(visitDTOList.get(0).getVisitId()))
				.andExpect(jsonPath("$.[1].visitId").value(visitDTOList.get(1).getVisitId()))
				.andExpect(jsonPath("$.[2].visitId").value(visitDTOList.get(2).getVisitId()));

		verify(visitsService, times(1)).getVisitsForPet(200, false);
	}

	@Test
	void shouldReturnListOfVisitsWhenFetchingScheduledVisitsWithValidPetId() throws Exception {
		when(visitsService.getVisitsForPet(anyInt(), anyBoolean())).thenReturn(visitDTOList);

		mvc.perform(get("/visits/scheduled/{petId}", 200))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[0].visitId").value(visitDTOList.get(0).getVisitId()))
				.andExpect(jsonPath("$.[1].visitId").value(visitDTOList.get(1).getVisitId()))
				.andExpect(jsonPath("$.[2].visitId").value(visitDTOList.get(2).getVisitId()));

		verify(visitsService, times(1)).getVisitsForPet(200, true);
	}

	@Test
	void shouldHandleInvalidInputExceptionWhenFetchingPreviousVisitsWithNegativePetId() throws Exception {
		when(visitsService.getVisitsForPet(anyInt(), anyBoolean()))
				.thenThrow(new InvalidInputException("PetId can't be negative."));

		mvc.perform(get("/visits/previous/{petId}", -1))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidInputException))
				.andExpect(result -> assertEquals("PetId can't be negative.", result.getResolvedException().getMessage()));
	}

	@Test
	void shouldHandleInvalidInputExceptionWhenFetchingScheduledVisitsWithNegativePetId() throws Exception {
		when(visitsService.getVisitsForPet(anyInt(), anyBoolean()))
				.thenThrow(new InvalidInputException("PetId can't be negative."));

		mvc.perform(get("/visits/scheduled/{petId}", -1))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidInputException))
				.andExpect(result -> assertEquals("PetId can't be negative.", result.getResolvedException().getMessage()));
	}

	@Test
	void shouldReturnBadRequestWhenFetchingScheduledVisitsWithInvalidPetId() throws Exception {
		mvc.perform(get("/visits/scheduled/{petId}", "invalid"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturnBadRequestWhenFetchingPreviousVisitsWithInvalidPetId() throws Exception {
		mvc.perform(get("/visits/previous/{petId}", "invalid"))
				.andExpect(status().isBadRequest());
	}

	// TESTS FOR FETCHING VISITS BASED ON PET ID ----------------------------------------------------------------------
	@Test
	void whenValidPetIdThenShouldReturnVisitsForPet() throws Exception {

		given(visitsService.getVisitsForPet(anyInt()))
				.willReturn(visitDTOList);

		mvc.perform(get("/visits/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].visitId").value(visitDTOList.get(0).getVisitId()))
				.andExpect(jsonPath("$[1].visitId").value(visitDTOList.get(1).getVisitId()))
				.andExpect(jsonPath("$[0].petId").value(visitDTOList.get(0).getPetId()))
				.andExpect(jsonPath("$[1].petId").value(visitDTOList.get(1).getPetId()));
	}

	@Test
	void shouldReturnBadRequestWhenFetchingVisitsWithInvalidPetId() throws Exception {
		mvc.perform(get("/visits/{petId}", "invalid"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void whenFetchingWithNegativePetIdShouldHandleInvalidInputException() throws Exception {
		when(visitsService.getVisitsForPet(-1)).thenThrow(new InvalidInputException("PetId can't be negative."));

		mvc.perform(get("/visits/{petId}", -1))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidInputException))
				.andExpect(result -> assertEquals("PetId can't be negative.", result.getResolvedException().getMessage()));
	}

	// TESTS FOR FETCHING VISITS BASED ON PET IDs ----------------------------------------------------------------------
	@Test
	void shouldFetchVisitsWhenValidPetIds() throws Exception {
		given(visitsService.getVisitsForPets(asList(111, 222)))
				.willReturn(visitDTOList);

		mvc.perform(get("/pets/visits?petId=111,222"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].visitId").value(visitDTOList.get(0).getVisitId()))
				.andExpect(jsonPath("$.items[1].visitId").value(visitDTOList.get(1).getVisitId()))
				.andExpect(jsonPath("$.items[2].visitId").value(visitDTOList.get(2).getVisitId()))
				.andExpect(jsonPath("$.items[0].petId").value(visitDTOList.get(0).getPetId()))
				.andExpect(jsonPath("$.items[1].petId").value(visitDTOList.get(1).getPetId()))
				.andExpect(jsonPath("$.items[2].petId").value(visitDTOList.get(2).getPetId()));
	}

	// TESTS FOR FETCHING VISITS BASED ON PRACTITIONER ID ----------------------------------------------------------------------
	@Test
	void shouldHandleInvalidInputExceptionWhenFetchingVisitsWithNegativePractitionerId() throws Exception {
		when(visitsService.getVisitsForPractitioner(-1)).thenThrow(new InvalidInputException("PractitionerId can't be negative."));

		mvc.perform(get("/visits/vets/{practitionerId}",-1))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidInputException))
				.andExpect(result -> assertEquals("PractitionerId can't be negative.", result.getResolvedException().getMessage()));
	}

	@Test
	void shouldReturnListOfVisitsWhenFetchingVisitsWithValidPractitionerId() throws Exception {
		given(visitsService.getVisitsForPractitioner(123456)).willReturn(visitDTOList);

		mvc.perform(get("/visits/vets/{practitionerId}",123456))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].practitionerId").value(123456))
				.andExpect(jsonPath("$[1].practitionerId").value(123456))
				.andExpect(jsonPath("$[2].practitionerId").value(123456));
	}


	// TESTS FOR FETCH VISITS BASED ON PRACTITIONER ID AND DATES ----------------------------------------------------------------------
	@Test
	void shouldReturnAllVisitsForSpecifiedPractitionerIdAndMonth() throws Exception {
		Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-01");
		Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-31");

		given(visitsService.getVisitsByPractitionerIdAndMonth(123456, startDate, endDate))
				.willReturn(visitDTOList);

		mvc.perform(get("/visits/calendar/{practitionnerId}?dates={startDate},{endDate}", 123456, "2021-10-01", "2021-10-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].practitionerId").value(123456))
				.andExpect(jsonPath("$[1].practitionerId").value(123456))
				.andExpect(jsonPath("$[2].practitionerId").value(123456));

		verify(visitsService, times(1)).getVisitsByPractitionerIdAndMonth(123456, startDate, endDate);
	}

	@Test
	void shouldHandleInvalidInputExceptionWhenFetchingWithNegativePractitionerId() throws Exception {
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


