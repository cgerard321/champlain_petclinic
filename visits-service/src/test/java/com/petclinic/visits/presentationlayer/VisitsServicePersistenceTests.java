package com.petclinic.visits.presentationlayer;
import com.petclinic.visits.datalayer.Visit;
import com.petclinic.visits.datalayer.VisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@Transactional(propagation  = Propagation.NOT_SUPPORTED)
@ActiveProfiles("test")
public class VisitsServicePersistenceTests {
    
    @Autowired
    private VisitRepository repository;
    
    @BeforeEach
    void cleanDb() {
        repository.deleteAll();
    }
    
    @Test
    @DisplayName("Create new visit")
    void createNewVisit() {
        Visit visit = new Visit(1, new Date(), "", 1, 123456);
        repository.save(visit);
        
        assertNotNull(visit);
        
        Visit createdVisit = repository.findByPetId(visit.getId()).get(0);
        
        assertEquals(visit.getId(), createdVisit.getId());
    }
}