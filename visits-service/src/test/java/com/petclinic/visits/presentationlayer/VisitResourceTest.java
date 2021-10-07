package com.petclinic.visits.presentationlayer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.visits.businesslayer.VisitsService;
import com.petclinic.visits.datalayer.Visit;
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

	@Autowired
	ControllerExceptionHandler exceptionHandler;

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

	// UTILS PACKAGE UNIT TESTING
	@Test
	void test_EmptyInvalidInputException(){
		InvalidInputException ex = assertThrows(InvalidInputException.class, ()->{
			throw new InvalidInputException();
		});
		assertEquals(ex.getMessage(), null);
	}

	@Test
	void test_ThrowableOnlyInvalidInputException(){
		InvalidInputException ex = assertThrows(InvalidInputException.class, ()->{
			throw new InvalidInputException(new Throwable());
		});
		assertEquals(ex.getCause().getMessage(), null);
	}

	@Test
	void testHandlerForInvalidInputException() throws JsonProcessingException {
		Visit expectedVisit = visit().id(1).petId(1).date(new Date()).description("").practitionerId(123456).build();

		HttpErrorInfo httpErrorInfo = exceptionHandler.handleInvalidInputException(MockServerHttpRequest.post("/owners/1/pets/{petId}/visits", 1)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(objectMapper.writeValueAsString(expectedVisit)), new InvalidInputException("Visit description required"));

		assertEquals(httpErrorInfo.getHttpStatus(), HttpStatus.UNPROCESSABLE_ENTITY);
		assertEquals(httpErrorInfo.getPath(), "/owners/1/pets/1/visits");
		assertEquals(httpErrorInfo.getTimeStamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")), ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")));
	}

	@Test
	void testHttpErrorInfoNullConstructor() throws JsonProcessingException {
		HttpErrorInfo httpErrorInfo = new HttpErrorInfo();

		assertEquals(httpErrorInfo.getHttpStatus(), null);
		assertEquals(httpErrorInfo.getPath(), null);
		assertEquals(httpErrorInfo.getTimeStamp(), null);
		assertEquals(httpErrorInfo.getMessage(), null);
	}

	@Test
	void testHandlerForNotFoundException() throws JsonProcessingException {
		Visit expectedVisit = visit().id(1).petId(65).date(new Date()).description("description").practitionerId(123456).build();

		HttpErrorInfo httpErrorInfo = exceptionHandler.handleNotFoundException(MockServerHttpRequest.post("/owners/1/pets/{petId}/visits", 1)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(objectMapper.writeValueAsString(expectedVisit)), new NotFoundException("Pet does not exist."));

		assertEquals(httpErrorInfo.getHttpStatus(), HttpStatus.NOT_FOUND);
		assertEquals(httpErrorInfo.getMessage(), "Pet does not exist.");
	}

	@Test
	void test_EmptyNotFoundException(){
		NotFoundException ex = assertThrows(NotFoundException.class, ()->{
			throw new NotFoundException();
		});
		assertEquals(ex.getMessage(), null);
		assertEquals(ex.getCause(), null);
	}

	@Test
	void test_ThrowableOnlyNotFoundException(){
		NotFoundException ex = assertThrows(NotFoundException.class, ()->{
			throw new NotFoundException(new Throwable("message"));
		});
		assertEquals(ex.getCause().getMessage(), "message");
	}

	@Test
	void test_MessageOnlyNotFoundException(){
		NotFoundException ex = assertThrows(NotFoundException.class, ()->{
			throw new NotFoundException("message");
		});
		assertEquals(ex.getMessage(), "message");
	}

	@Test
	void test_ThrowableMessageNotFoundException(){
		NotFoundException ex = assertThrows(NotFoundException.class, ()->{
			throw new NotFoundException("message", new Throwable("message"));
		});
		assertEquals(ex.getCause().getMessage(), "message");
		assertEquals(ex.getMessage(), "message");
	}
}


