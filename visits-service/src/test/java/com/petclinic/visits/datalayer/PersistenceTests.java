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

    @Autowired
    private VisitRepository repo;
    private Visit visit;

    @BeforeEach
    public void setupDb() throws ParseException {
        repo.deleteAll();

        // add setup data here
        visit = Visit.visit()
                .id(1)
                .petId(1)
                .practitionerId(200200)
                .date(new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-02"))
                .status(true)
                .build();
        repo.save(visit);
    }

    @Test
    public void getVisitsForPet() {
        List<Visit> repoResponse = repo.findByPetId(1);
        assertThat(repoResponse, hasSize(1));
    }

    @Test
    public void shouldReturnEmptyListWhenPetDoesNotExist(){
        List<Visit> repoResponse = repo.findByPetId(2);
        assertThat(repoResponse, hasSize(0));
    }
    
    @Test
    public void confirmAndCancelAppointment(){
        Visit v = visit().petId(1).status(false).build();
        repo.save(v);

        List<Visit> repoResponse = repo.findByPetId(1);

        assertThat(repoResponse, hasSize(2));
        assertThat(repoResponse.get(0).isStatus(), equalTo(true));
        assertThat(repoResponse.get(1).isStatus(), equalTo(false));
    }

    
    @Test
    public void createVisitForPet() {
        Visit visit = visit().petId(3).date(new Date()).description("").practitionerId(123456).build();

        repo.save(visit);
        List<Visit> repoResponse = repo.findByPetId(3);
        assertThat(repoResponse, hasSize(1));
    }


    @Test
    public void getVisitsForNonExistentPet(){
        List<Visit> repoResponse = repo.findByPetId(0);
        assertThat(repoResponse, hasSize(0));
    }

  
    @Test
    public void Is_deleting_Visit () {
        repo.delete(visit);
        assertFalse(repo.existsById(visit.getId()));
    }

    @Test
    public void Is_Visit_Empty_Dont_Delete() {
        Visit visit = new Visit();
        Visit v = visit().petId(1).build();
        repo.save(v);

        repo.delete(visit);
        assertEquals(repo.findByPetId(1).size(), 2);
    }

    @Test
    public void updateVisit(){
        Visit savedVisit = new Visit(5, UUID.randomUUID(), new Date(), "Description", 5, 123456, true);
        savedVisit = repo.save(savedVisit);

        savedVisit.setDescription("Updated Description");
        repo.save(savedVisit);
        
        Visit foundVisit = repo.findById(savedVisit.getId()).get();
        assertEquals("Updated Description", foundVisit.getDescription());
    }

    @Test
    public void findByPractitionerId(){
        List<Visit> returnedVisits = repo.findVisitsByPractitionerId(200200);
        assertEquals(1, returnedVisits.size());
    }

    @Test
    public void findByNonExistentPractitionerId(){
        List<Visit> returnedVisits = repo.findVisitsByPractitionerId(234234);
        assertEquals(0, returnedVisits.size());
    }

    @Test
    public void getVisitsByPractitionerIdAndMonth() throws ParseException {
        Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-01");
        Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-31");

        Visit visitDuring1 = new Visit(123, UUID.randomUUID(), new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-01"), "Description", 2, 123456, true);
        repo.save(visitDuring1);

        Visit visitDuring2 = new Visit(122, UUID.randomUUID(), new SimpleDateFormat("yyyy-MM-dd").parse("2021-10-31"), "Description", 2, 123456, true);
        repo.save(visitDuring2);

        Visit visitAfter = new Visit(121, UUID.randomUUID(), new SimpleDateFormat("yyyy-MM-dd").parse("2021-11-01"), "Description", 2, 123456, true);
        repo.save(visitAfter);

        Visit visitBefore = new Visit(120, UUID.randomUUID(), new SimpleDateFormat("yyyy-MM-dd").parse("2021-09-30"), "Description", 2, 123456, true);
        repo.save(visitBefore);

        List<Visit> repoResponse = repo.findAllByDateBetween(startDate, endDate);

        assertEquals(3, repoResponse.size());
    }
}

