package com.petclinic.visits.businesslayer;

import com.petclinic.visits.datalayer.Visit;
import com.petclinic.visits.datalayer.VisitDTO;
import com.petclinic.visits.datalayer.VisitIdLessDTO;
import com.petclinic.visits.datalayer.VisitRepository;
import com.petclinic.visits.utils.exceptions.InvalidInputException;
import com.petclinic.visits.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static com.petclinic.visits.datalayer.Visit.visit;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


import static org.mockito.Mockito.*;

import static org.mockito.Mockito.when;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import javax.swing.text.html.Option;
import java.io.Console;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@SpringBootTest
@ExtendWith(SpringExtension.class)
public class VisitsServiceImplTests {

    @MockBean
    VisitRepository repo;

    @Autowired
    VisitsService visitsService;

    VisitIdLessDTO visitIdLessDTOConfirmed = new VisitIdLessDTO(
            new Date(System.currentTimeMillis()),
            "Description",
            200,
            123456,
            true
    );

    VisitIdLessDTO visitIdLessDTOCanceled = new VisitIdLessDTO(
            new Date(System.currentTimeMillis()),
            "Description",
            200,
            123456,
            false
    );

    VisitDTO visitDTO = new VisitDTO(UUID.randomUUID().toString(),
            new Date(System.currentTimeMillis()),
            "Description",
            200,
            123456,
            true);

    Visit visitEntity = new Visit(1, UUID.fromString(visitDTO.getVisitId()),
            new Date(System.currentTimeMillis()),
            "Description",
            200,
            123456,
            true);


    // TESTS FOR UPDATING A VISIT ----------------------------------------------------------------------
    @Test
    public void whenValidIdUpdateVisit(){
        when(repo.findByVisitId(any())).thenReturn(Optional.ofNullable(visitEntity));
        when(repo.save(any(Visit.class))).thenReturn(visitEntity);

        VisitDTO visitFromService = visitsService.updateVisit(visitDTO);

        assertEquals(visitDTO.getVisitId(), visitFromService.getVisitId());
        assertEquals(visitDTO.getDate(), visitFromService.getDate());
        assertEquals(visitDTO.getDescription(), visitFromService.getDescription());
        assertEquals(visitDTO.getPetId(), visitFromService.getPetId());
        assertEquals(visitDTO.getPractitionerId(), visitFromService.getPractitionerId());
        assertEquals(visitDTO.isStatus(), visitFromService.isStatus());
    }

    // should add tests for error handling and validation

    // TESTS FOR DELETING A VISIT ----------------------------------------------------------------------
    @Test
    public void shouldCallRepoDeleteVisitWhenDeletingWithValidVisitId(){
        Visit visit = new Visit(1, UUID.randomUUID(), new Date(System.currentTimeMillis()), "Description", 200, 123456, true);
        when(repo.findByVisitId(visit.getVisitId())).thenReturn(Optional.of(visit));
        visitsService.deleteVisit(visit.getVisitId().toString());
        verify(repo, times(1)).delete(visit);
    }

    @Test
    public void shouldNotCallRepoDeleteVisitWhenDeletingWithNonExistentVisitId(){
        Visit visit = new Visit(1, UUID.randomUUID(), new Date(System.currentTimeMillis()), "Description", 200, 123456, true);
        when(repo.findByVisitId(visit.getVisitId())).thenReturn(Optional.of(new Visit()));
        visitsService.deleteVisit(visit.getVisitId().toString());
        verify(repo, never()).delete(visit);
    }


    // should add tests for error handling and validation
  
    // TESTS FOR GETTING A SINGLE VISIT ----------------------------------------------------------------------
    @Test
    public void shouldReturnVisitWhenFetchingWithValidVisitId() {
        when(repo.findByVisitId(any())).thenReturn(Optional.of(visitEntity));

        VisitDTO visitFromService = visitsService.getVisitByVisitId(visitEntity.getVisitId().toString());

        assertEquals(visitEntity.getVisitId().toString(), visitFromService.getVisitId());
    }

    @Test
    public void shouldThrowInvalidInputExceptionWhenInvalidVisitId() {
        InvalidInputException invalidInputException = assertThrows(InvalidInputException.class, () ->{
            visitsService.getVisitByVisitId("invalid");
        });

        assertEquals("VisitId not in the right format.", invalidInputException.getMessage());
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenFetchingVisitWithNonExistentVisitId(){
        when(repo.findByVisitId(any())).thenReturn(Optional.of(new Visit()));
        String randomId = UUID.randomUUID().toString();

        NotFoundException ex = assertThrows(NotFoundException.class, () ->{
           visitsService.getVisitByVisitId(randomId);
        });

        assertEquals("Visit with visitId: " + randomId + " does not exist.", ex.getMessage());
    }

    @Test
    public void shouldReturnTrueWhenGivenValidUUID(){
        boolean valid = visitsService.validateVisitId(UUID.randomUUID().toString());
        assertTrue(valid);
    }

    @Test
    public void shouldReturnFalseWhenGivenInvalidUUID(){
        boolean valid = visitsService.validateVisitId("invalid");
        assertFalse(valid);
    }
  
    // TESTS FOR CREATING A VISIT ----------------------------------------------------------------------
    @Test
    public void shouldCreateConfirmedVisitWhenValidPetId() {
        Visit createdVisit = visit()
                .visitId(UUID.randomUUID())
                .date(new Date())
                .description("Description")
                .petId(200)
                .practitionerId(123456)
                .status(true).build();

        when(repo.save(any(Visit.class))).thenReturn(createdVisit);

        VisitDTO serviceResponse = visitsService.addVisit(visitIdLessDTOConfirmed);

        assertEquals(serviceResponse.getVisitId(), createdVisit.getVisitId().toString());
        assertEquals(serviceResponse.getDate(), createdVisit.getDate());
        assertEquals(serviceResponse.getDescription(), createdVisit.getDescription());
        assertEquals(serviceResponse.getPetId(), createdVisit.getPetId());
        assertEquals(serviceResponse.getPractitionerId(), createdVisit.getPractitionerId());
        assertEquals(serviceResponse.isStatus(), createdVisit.isStatus());
    }

    @Test
    public void shouldCreateCanceledVisitWhenValidPetId() {
        Visit createdVisit = visit()
                .visitId(UUID.randomUUID())
                .date(new Date())
                .description("Description")
                .petId(200)
                .practitionerId(123456)
                .status(false).build();

        when(repo.save(any(Visit.class))).thenReturn(createdVisit);

        VisitDTO serviceResponse = visitsService.addVisit(visitIdLessDTOCanceled);

        assertEquals(serviceResponse.getVisitId(), createdVisit.getVisitId().toString());
        assertEquals(serviceResponse.getDate(), createdVisit.getDate());
        assertEquals(serviceResponse.getDescription(), createdVisit.getDescription());
        assertEquals(serviceResponse.getPetId(), createdVisit.getPetId());
        assertEquals(serviceResponse.getPractitionerId(), createdVisit.getPractitionerId());
        assertEquals(serviceResponse.isStatus(), createdVisit.isStatus());
    }

    @Test
    public void shouldThrowInvalidInputExceptionWhenCreatingVisitWithEmptyDescription(){
        // arrange
        String expectedExceptionMessage = "Visit description required.";
        visitIdLessDTOConfirmed.setDescription("");

        // act and assert
        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->{
            visitsService.addVisit(visitIdLessDTOConfirmed);
        });
        assertEquals(ex.getMessage(), expectedExceptionMessage);
    }

    @Test
    public void shouldThrowInvalidInputExceptionWhenCreatingVisitWithNullDescription(){
        // arrange
        String expectedExceptionMessage = "Visit description required.";
        visitIdLessDTOConfirmed.setDescription(null);

        // act and assert
        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->{
            visitsService.addVisit(visitIdLessDTOConfirmed);
        });
        assertEquals(ex.getMessage(), expectedExceptionMessage);
    }

    @Test
    public void shouldThrowInvalidInputExceptionWhenVisitIdAlreadyExists(){
        // arrange
        String expectedExceptionMessage = "Duplicate visitId.";
        when(repo.save(any(Visit.class))).thenThrow(DuplicateKeyException.class);

        // act and assert
        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->{
            visitsService.addVisit(visitIdLessDTOConfirmed);
        });
        assertEquals(ex.getMessage(), expectedExceptionMessage);
        assertThat(ex.getCause()).isInstanceOf(DuplicateKeyException.class);
    }

    // should add tests for error handling and validation

    // TESTS FOR FETCHING VISITS BASED ON PET ID ----------------------------------------------------------------------
    @Test
    public void shouldReturnVisitsForPetWhenValidPetId(){
        when(repo.findByPetId(anyInt())).thenReturn(
                asList(
                        visit()
                                .visitId(UUID.randomUUID())
                                .petId(200)
                                .build(),
                        visit()
                                .visitId(UUID.randomUUID())
                                .petId(200)
                                .build()
                )
        );

        List<VisitDTO> serviceResponse = visitsService.getVisitsForPet(200);

        assertThat(serviceResponse, hasSize(2));
        assertThat(serviceResponse.get(1).getPetId(), equalTo(200));
    }

    @Test
    public void shouldThrowInvalidInputExceptionWhenNegativePetId(){
        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->{
           visitsService.getVisitsForPet(-1);
        });

        assertEquals("PetId can't be negative.", ex.getMessage());
    }

    // should add tests for error handling and validation

    // TESTS FOR FETCHING VISITS BASED ON PET IDS ----------------------------------------------------------------------
    @Test
    public void shouldReturnVisitsForPetAsListWhenValidPetIdThen(){
        List<Visit> visitsList = asList(
                visit()
                        .id(1)
                        .petId(1)
                        .build(),
                visit()
                        .id(2)
                        .petId(1)
                        .build());

        ArrayList<Integer> petIdsToSearchFor = new ArrayList<>();
        petIdsToSearchFor.add(1);

        when(repo.findByPetIdIn(anyList())).thenReturn(visitsList);

        List<VisitDTO> serviceResponse = visitsService.getVisitsForPets(petIdsToSearchFor);

        assertEquals(2, serviceResponse.size());
    }

    // should add tests for error handling and validation

    // TESTS FOR FETCH VISITS BASED ON PRACTITIONER ID AND DATES ----------------------------------------------------------------------
    @Test
    public void shouldReturnAllVisitsForPractitionerId() throws ParseException {
        Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-01");
        Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-31");

        List<Visit> visitsList = asList(
                visit()
                        .id(1)
                        .visitId(UUID.randomUUID())
                        .petId(1)
                        .date(new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-01"))
                        .practitionerId(1)
                        .build(),
                visit()
                        .id(2)
                        .visitId(UUID.randomUUID())
                        .petId(1)
                        .date(new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-06"))
                        .practitionerId(1)
                        .build(),
                visit()
                        .id(3)
                        .visitId(UUID.randomUUID())
                        .petId(1)
                        .date(new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-25"))
                        .practitionerId(2)
                        .build(),
                visit()
                        .id(4)
                        .visitId(UUID.randomUUID())
                        .petId(1)
                        .date(new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-25"))
                        .practitionerId(3)
                        .build());


        when(repo.findAllByDateBetween(startDate, endDate)).thenReturn(visitsList);

        List<VisitDTO> returnedVisits = visitsService.getVisitsByPractitionerIdAndMonth(1, startDate, endDate);

        assertEquals(2, returnedVisits.size());
        assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-01"), returnedVisits.get(0).getDate());
        assertEquals(1, returnedVisits.get(1).getPractitionerId());
    }

    @Test
    public void shouldThrowInvalidInputExceptionWhenFetchingWithNegativePractitionerId() throws ParseException {
        Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-01");
        Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-31");

        InvalidInputException ex = assertThrows(InvalidInputException.class, () -> {
            visitsService.getVisitsByPractitionerIdAndMonth(-1, startDate, endDate);
        });

        assertEquals("PractitionerId can't be negative.", ex.getMessage());
    }

    // should add tests for error handling and validation

    // TESTS FOR FETCHING VISITS BASED ON DATE ----------------------------------------------------------------------
    @Test
    public void shouldReturnVisitsAfterNowWhenFetchingScheduledVisits() throws ParseException {
        Date afterNow = new Date(System.currentTimeMillis() + 100000000); //
        Date beforeNow = new Date(System.currentTimeMillis() - 100000000);

        List<Visit> visitsList = asList(
                visit()
                        .visitId(UUID.randomUUID())
                        .petId(200)
                        .date(afterNow)
                        .build(),
                visit()
                        .visitId(UUID.randomUUID())
                        .petId(200)
                        .date(afterNow)
                        .build(),
                visit()
                        .visitId(UUID.randomUUID())
                        .petId(200)
                        .date(beforeNow)
                        .build());

        when(repo.findByPetId(anyInt())).thenReturn(visitsList);

        List<VisitDTO> returnedVisits = visitsService.getVisitsForPet(200, true);

        assertEquals(2, returnedVisits.size());

    }

    @Test
    public void shouldReturnVisitsBeforeNow() throws ParseException {
        Date afterNow = new Date(System.currentTimeMillis() + 100000000);
        Date beforeNow = new Date(System.currentTimeMillis() - 100000000);

        List<Visit> visitsList = asList(
                visit()
                        .visitId(UUID.randomUUID())
                        .petId(200)
                        .date(afterNow)
                        .build(),
                visit()
                        .visitId(UUID.randomUUID())
                        .petId(200)
                        .date(afterNow)
                        .build(),
                visit()
                        .visitId(UUID.randomUUID())
                        .petId(200)
                        .date(beforeNow)
                        .build());

        when(repo.findByPetId(anyInt())).thenReturn(visitsList);

        List<VisitDTO> returnedVisits = visitsService.getVisitsForPet(200, false);

        assertEquals(1, returnedVisits.size());
    }

    @Test
    public void shouldReturnEmptyListWhenNoScheduledVisits(){
        Date beforeNow = new Date(System.currentTimeMillis() - 100000000);

        List<Visit> visitsList = asList(
                visit()
                        .visitId(UUID.randomUUID())
                        .petId(200)
                        .date(beforeNow)
                        .build(),
                visit()
                        .visitId(UUID.randomUUID())
                        .petId(200)
                        .date(beforeNow)
                        .build(),
                visit()
                        .visitId(UUID.randomUUID())
                        .petId(200)
                        .date(beforeNow)
                        .build());

        when(repo.findByPetId(anyInt())).thenReturn(visitsList);

        List<VisitDTO> returnedVisits = visitsService.getVisitsForPet(200, true);

        assertEquals(0, returnedVisits.size());
    }

    @Test
    public void shouldReturnEmptyListWhenNoPreviousVisits(){
        Date afterNow = new Date(System.currentTimeMillis() + 100000000);

        List<Visit> visitsList = asList(
                visit()
                        .visitId(UUID.randomUUID())
                        .petId(200)
                        .date(afterNow)
                        .build(),
                visit()
                        .visitId(UUID.randomUUID())
                        .petId(200)
                        .date(afterNow)
                        .build(),
                visit()
                        .visitId(UUID.randomUUID())
                        .petId(200)
                        .date(afterNow)
                        .build());

        when(repo.findByPetId(anyInt())).thenReturn(visitsList);

        List<VisitDTO> returnedVisits = visitsService.getVisitsForPet(200, false);

        assertEquals(0, returnedVisits.size());
    }

    @Test
    public void shouldThrowInvalidInputExceptionWhenFetchingVisitsWithNegativePetId(){
        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->{
            visitsService.getVisitsForPet(-1, true);
        });

        assertEquals("PetId can't be negative.", ex.getMessage());
    }

    // should add tests for error handling and validation

    // TESTS FOR FETCHING VISITS BASED ON PRACTITIONER ID ----------------------------------------------------------------------
    @Test
    public void shouldThrowInvalidInputExceptionWhenFetchingVisitsWithNegativePractitionerId(){
        InvalidInputException ex = assertThrows(InvalidInputException.class, ()->{
           visitsService.getVisitsForPractitioner(-1);
        });

        assertEquals("PractitionerId can't be negative.", ex.getMessage());
    }

    @Test
    public void shouldReturnEmptyListWhenFetchingDatesForPractitionerWithNoVisits(){
        List<Visit> repoResponse = new ArrayList<Visit>();
        when(repo.findVisitsByPractitionerId(anyInt())).thenReturn(repoResponse);

        List<VisitDTO> returnedVisits = visitsService.getVisitsForPractitioner(404);

        assertEquals(0, returnedVisits.size());
    }

    @Test
    public void shouldReturnListOfVisitsWhenFetchingWithValidPractitionerId() throws ParseException {
        List<Visit> visitsList = asList(
                visit()
                        .id(1)
                        .petId(1)
                        .date(new SimpleDateFormat("yyyy-MM-dd").parse("2020-03-04"))
                        .practitionerId(200200)
                        .build(),
                visit()
                        .id(3)
                        .petId(1)
                        .date(new SimpleDateFormat("yyyy-MM-dd").parse("2021-03-04"))
                        .practitionerId(200200)
                        .build(),
                visit()
                        .id(2)
                        .petId(1)
                        .date(new SimpleDateFormat("yyyy-MM-dd").parse("2022-03-04"))
                        .practitionerId(200200)
                        .build());

        when(repo.findVisitsByPractitionerId(anyInt())).thenReturn(visitsList);

        List<VisitDTO> returnedVisits = visitsService.getVisitsForPractitioner(200200);

        assertEquals(3, returnedVisits.size());
        assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("2021-03-04"), returnedVisits.get(1).getDate());
    }

    // should add tests for error handling and validation
}
