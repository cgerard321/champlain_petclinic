package com.petclinic.visits.datalayer;

import com.petclinic.visits.businesslayer.VisitsService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.petclinic.visits.datalayer.Visit.visit;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@Slf4j
@ExtendWith(SpringExtension.class)
@DataJpaTest
@Transactional(propagation = NOT_SUPPORTED)
public class PersistenceTests {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private VisitRepository repo;
    private Visit visit;

    @BeforeEach
    public void setupDb() throws ParseException {
        repo.deleteAll();

        // add setup data here
        visit = Visit.visit()
                .visitId(UUID.randomUUID())
                .date(new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-02"))
                .description("Description")
                .petId(200)
                .practitionerId(200200)
                .status(true)
                .build();
        repo.save(visit);
    }

    // TESTS FOR UPDATING A VISIT ----------------------------------------------------------------------
    @Test
    public void updateVisit(){
        Visit savedVisit = new Visit(5, UUID.randomUUID(), new Date(), "Description", 5, 123456, true);
        savedVisit = repo.save(savedVisit);

        savedVisit.setDescription("Updated Description");
        repo.save(savedVisit);

        Visit foundVisit = repo.findById(savedVisit.getId()).get();
        assertEquals("Updated Description", foundVisit.getDescription());
    }

    // TESTS FOR DELETING A VISIT ----------------------------------------------------------------------
    @Test
    public void shouldDeleteVisitWhenPassingValidEntity() {
        repo.delete(visit);
        assertFalse(repo.existsById(visit.getId()));
    }

    @Test
    public void shouldNotDeleteVisitWhenPassingEntityWithDifferentId() {
        Visit v = new Visit();
        v.setId(2);

        repo.delete(v);
        assertEquals(1, repo.findAll().size());
    }

    // TESTS FOR FETCHING VISIT BASED ON VISIT ID ----------------------------------------------------------------------
    @Test
    public void shouldFindVisitByVisitIdWhenExistingVisit(){
        Optional<Visit> v = repo.findByVisitId(visit.getVisitId());

        assertEquals(v.get().getId(), visit.getId());
        assertEquals(v.get().getVisitId(), visit.getVisitId());
        assertEquals(sdf.format(v.get().getDate()), sdf.format(visit.getDate()));
        assertEquals(v.get().getDescription(), visit.getDescription());
        assertEquals(v.get().getPetId(), visit.getPetId());
        assertEquals(v.get().getPractitionerId(), visit.getPractitionerId());
        assertEquals(v.get().isStatus(), visit.isStatus());
    }

    @Test
    public void shouldThrowNoSuchElementExceptionWhenNonExistingVisit(){
        Optional<Visit> v = repo.findByVisitId(UUID.randomUUID());

        assertThrows(NoSuchElementException.class, ()->{
           v.get();
        });
    }

    // TESTS FOR CREATING A VISIT ----------------------------------------------------------------------
    @Test
    public void createVisitForPet() {
        Visit v = visit()
                .visitId(UUID.randomUUID())
                .date(new Date(System.currentTimeMillis()))
                .description("Description")
                .petId(200)
                .practitionerId(200200)
                .status(false).build();

        Visit savedVisit = repo.save(v);

        assertEquals(v.getVisitId(), savedVisit.getVisitId());
        assertEquals(v.getDate(), savedVisit.getDate());
        assertEquals(v.getDescription(), savedVisit.getDescription());
        assertEquals(v.getPetId(), savedVisit.getPetId());
        assertEquals(v.getPractitionerId(), savedVisit.getPractitionerId());
        assertEquals(v.isStatus(), savedVisit.isStatus());
    }

    // TESTS FOR FETCHING A SINGLE VISIT BY VISIT ID ----------------------------------------------------------------------
    @Test
    public void shouldGetVisitWhenFetchingVisitWithExistingVisitId() {
        Visit visit = new Visit(5, UUID.randomUUID(), new Date(), "Description", 5, 123456, true);
        visit = repo.save(visit);

        Visit foundVisit = repo.findByVisitId(visit.getVisitId()).get();

        assertEquals(visit.getId(), foundVisit.getId());
        assertEquals(visit.getPetId(), foundVisit.getPetId());
        assertEquals(visit.isStatus(), foundVisit.isStatus());
    }

    @Test
    public void shouldThrowNoSuchElementExceptionWhenFetchingVisitWithNonExistentVisitId(){
        assertThrows(NoSuchElementException.class, ()->{
           repo.findByVisitId(UUID.randomUUID()).get();
        });
    }
  
    // TESTS FOR FETCHING VISITS BASED ON PET ID ----------------------------------------------------------------------
    @Test
    public void shouldReturnVisitsForPetWhenFetchingVisitsWithExistingPetId() {
        List<Visit> repoResponse = repo.findByPetId(200);
        assertThat(repoResponse, hasSize(1));

    }

    @Test
    public void shouldReturnEmptyListWhenNoVisitsWithPetId(){
        List<Visit> repoResponse = repo.findByPetId(404);
        assertThat(repoResponse, hasSize(0));
    }

    // TESTS FOR FETCHING VISITS BASED ON PET IDS ----------------------------------------------------------------------
    @Test
    public void shouldFetchVisitsOfPetsWithPetIds() {
        Visit v = visit()
                .visitId(UUID.randomUUID())
                .date(new Date(System.currentTimeMillis()))
                .description("Description")
                .petId(201)
                .practitionerId(200200)
                .status(false).build();
        repo.save(v);

        List<Integer> petIds = Arrays.asList(200, 201);
        List<Visit> repoResponse = repo.findByPetIdIn(petIds);
        assertEquals(2, repoResponse.size());
        assertEquals(200, repoResponse.get(0).getPetId());
        assertEquals(201, repoResponse.get(1).getPetId());
    }

    // TESTS FOR FETCH VISITS BASED ON PRACTITIONER ID AND DATES ----------------------------------------------------------------------
    @Test
    public void getVisitsByPractitionerIdAndMonth() throws ParseException {
        Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-01");
        Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-31");

        Visit visitDuring1 = new Visit(123, UUID.randomUUID(),
                new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-01"),
                "Description", 2, 123456, true);
        repo.save(visitDuring1);

        Visit visitDuring2 = new Visit(122, UUID.randomUUID(),
                new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-31"),
                "Description", 2, 123456, true);
        repo.save(visitDuring2);

        Visit visitAfter = new Visit(121, UUID.randomUUID(),
                new SimpleDateFormat("yyyy-MM-dd").parse("2021-11-01"),
                "Description", 2, 123456, true);
        repo.save(visitAfter);

        Visit visitBefore = new Visit(120, UUID.randomUUID(),
                new SimpleDateFormat("yyyy-MM-dd").parse("2021-09-30"),
                "Description", 2, 123456, true);
        repo.save(visitBefore);

        List<Visit> repoResponse = repo.findAllByDateBetween(startDate, endDate);

        assertEquals(3, repoResponse.size());
    }

    // TESTS FOR FETCHING VISITS BASED ON PRACTITIONER ID ----------------------------------------------------------------------
    @Test
    public void shouldReturnVisitsWhenFetchingWithExistingPractitionerId(){
        Visit v = visit()
                .visitId(UUID.randomUUID())
                .date(new Date(System.currentTimeMillis()))
                .description("Description")
                .petId(200)
                .practitionerId(200200)
                .status(false).build();

        Visit savedVisit = repo.save(v);

        List<Visit> returnedVisits = repo.findVisitsByPractitionerId(200200);
        assertEquals(2, returnedVisits.size());
    }

    @Test
    public void shouldReturnEmptyListWhenFetchingVisitsForNonExistentPractitioner(){
        List<Visit> returnedVisits = repo.findVisitsByPractitionerId(234234);
        assertEquals(0, returnedVisits.size());
    }
}

