package com.petclinic.visits.businesslayer;

import com.petclinic.visits.datalayer.Visit;
import com.petclinic.visits.datalayer.VisitRepository;
import com.petclinic.visits.utils.exceptions.InvalidInputException;
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

    private Visit visit;

    @BeforeEach
    public void setupDb(){
        repo.deleteAll();

        // add setup data here
        visit = Visit.visit()
                .id(1)
                .petId(1)
                .build();
        Visit visit1 = Visit.visit()
                .id(2)
                .petId(1)
                .build();

        List<Visit> list = Arrays.asList(visit, visit1);
        repo.saveAll(list);
    }
    
    
    
    @Test
    public void whenValidPetIdThenShouldReturnVisitsForPet(){
        when(repo.findByPetId(1)).thenReturn(
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
        
        List<Visit> serviceResponse = visitsService.getVisitsForPet(1);
        
        assertThat(serviceResponse, hasSize(2));
        assertThat(serviceResponse.get(1).getPetId(), equalTo(1));
    }
    
    @Test
    public void whenValidPetIdThenShouldReturnVisitsForPetAsList(){
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
        
        List<Visit> serviceResponse = visitsService.getVisitsForPets(petIdsToSearchFor);
        
        assertArrayEquals(visitsList.toArray(), serviceResponse.toArray());
    }
    
    @Test
    public void whenValidIdUpdateVisit(){
        Visit updatedVisit = visit().petId(1).date(new Date()).description("Desc-1 Updated").build();

        when(repo.save(any(Visit.class))).thenReturn(updatedVisit);

        Visit visitFromService = visitsService.updateVisit(updatedVisit);

        assertThat(visitFromService.getDescription(), equalTo("Desc-1 Updated"));
    }

    @Test
    public void whenValidPetIdThenCreateConfirmedVisitForPet(){
        Visit visit = visit().id(2).petId(1).status(true).build();

        when(repo.save(visit)).thenReturn(visit);

        Visit serviceResponse = repo.save(visit);

        assertThat(serviceResponse.getPetId(), equalTo(1));
        assertThat(serviceResponse.isStatus(), equalTo(true));
    }
  
    @Test
    public void whenValidPetIdThenShouldCreateVisitForPet() {
        Visit createdVisit = visit().petId(1).date(new Date()).description("Description").practitionerId(123456).build();
        
        when(repo.save(any(Visit.class))).thenReturn(createdVisit);
        
        Visit serviceResponse = visitsService.addVisit(createdVisit);
        
        assertThat(serviceResponse.getPetId(), equalTo(createdVisit.getPetId()));
    }

    @Test
    public void whenEmptyDescriptionThenShouldThrowInvalidInputException(){
        // arrange
        String expectedExceptionMessage = "Visit description required.";
        Visit createdVisit = visit().petId(1).date(new Date()).description("").practitionerId(123456).build();
        when(repo.save(any(Visit.class))).thenReturn(createdVisit);

        // act and assert
        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->{
            visitsService.addVisit(createdVisit);
        });
        assertEquals(ex.getMessage(), expectedExceptionMessage);
    }


    @Test
    public void whenVisitIdAlreadyExistsThenThrowInvalidInputException(){
        // arrange
        String expectedExceptionMessage;
        Visit createdVisit = visit().petId(1).date(new Date()).description("Description").practitionerId(123456).build();
        when(repo.save(any(Visit.class))).thenThrow(DuplicateKeyException.class);

        // act and assert
        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->{
            visitsService.addVisit(createdVisit);
        });
        expectedExceptionMessage = "Duplicate visitId: " + createdVisit.getId();
        assertEquals(ex.getMessage(), expectedExceptionMessage);
        assertThat(ex.getCause()).isInstanceOf(DuplicateKeyException.class);
    }


    @Test
    public void whenValidPetIdThenCreateCanceledVisitForPet() {
        Visit visit = visit().id(2).petId(1).status(false).build();

        when(repo.save(visit)).thenReturn(visit);

        Visit serviceResponse = repo.save(visit);

        assertThat(serviceResponse.getPetId(), equalTo(1));
        assertThat(serviceResponse.isStatus(), equalTo(false));
    }
    
    @Test
    public void whenValidIdDeleteVisit(){
        Visit vise = new Visit(1, new Date(System.currentTimeMillis()), "Cancer", 1);
        when(repo.findById(1)).thenReturn(Optional.of(vise));
        visitsService.deleteVisit(1);
        verify(repo, times(1)).delete(vise);
    }

    @Test
    public void whenVisitDoNotExist(){
        Visit vise = new Visit(3, new Date(System.currentTimeMillis()), "Cancer", 1);
        visitsService.deleteVisit(3);
        verify(repo, never()).delete(vise);
    }

    // TESTS FOR FETCHING VISITS BASED ON DATE
    @Test
    public void shouldReturnVisitsAfterNow() throws ParseException {
        Date afterNow = new Date(System.currentTimeMillis() + 100000);
        Date beforeNow = new Date(System.currentTimeMillis() - 100000);

        List<Visit> visitsList = asList(
                visit()
                        .id(1)
                        .petId(1)
                        .date(afterNow)
                        .build(),
                visit()
                        .id(3)
                        .petId(1)
                        .date(afterNow)
                        .build(),
                visit()
                        .id(2)
                        .petId(1)
                        .date(beforeNow)
                        .build());

        when(repo.findByPetId(1)).thenReturn(visitsList);

        List<Visit> returnedVisits = visitsService.getVisitsForPet(1, true);

        assertEquals(2, returnedVisits.size());

    }

    @Test
    public void shouldReturnVisitsBeforeNow() throws ParseException {
        Date afterNow = new Date(System.currentTimeMillis() + 100000);
        Date beforeNow = new Date(System.currentTimeMillis() - 100000);

        List<Visit> visitsList = asList(
                visit()
                        .id(1)
                        .petId(1)
                        .date(afterNow)
                        .build(),
                visit()
                        .id(3)
                        .petId(1)
                        .date(afterNow)
                        .build(),
                visit()
                        .id(2)
                        .petId(1)
                        .date(beforeNow)
                        .build());

        when(repo.findByPetId(1)).thenReturn(visitsList);

        List<Visit> returnedVisits = visitsService.getVisitsForPet(1, false);

        assertEquals(1, returnedVisits.size());

    }

    @Test
    public void shouldReturnEmptyListWhenNoScheduledVisits(){
        Date beforeNow = new Date(System.currentTimeMillis() - 100000);

        List<Visit> visitsList = asList(
                visit()
                        .id(1)
                        .petId(1)
                        .date(beforeNow)
                        .build(),
                visit()
                        .id(3)
                        .petId(1)
                        .date(beforeNow)
                        .build(),
                visit()
                        .id(2)
                        .petId(1)
                        .date(beforeNow)
                        .build());

        when(repo.findByPetId(1)).thenReturn(visitsList);

        List<Visit> returnedVisits = visitsService.getVisitsForPet(1, true);

        assertEquals(0, returnedVisits.size());
    }

    @Test
    public void shouldReturnEmptyListWhenNoPreviousVisits(){
        Date afterNow = new Date(System.currentTimeMillis() + 100000);

        List<Visit> visitsList = asList(
                visit()
                        .id(1)
                        .petId(1)
                        .date(afterNow)
                        .build(),
                visit()
                        .id(3)
                        .petId(1)
                        .date(afterNow)
                        .build(),
                visit()
                        .id(2)
                        .petId(1)
                        .date(afterNow)
                        .build());

        when(repo.findByPetId(1)).thenReturn(visitsList);

        List<Visit> returnedVisits = visitsService.getVisitsForPet(1, false);

        assertEquals(0, returnedVisits.size());
    }

    @Test
    public void shouldThrowInvalidInputExceptionWhenFetchingWithNegativePetId(){
        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->{
            visitsService.getVisitsForPet(-1, true);
        });

        assertEquals("PetId can't be negative.", ex.getMessage());
    }



}
