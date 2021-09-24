package com.petclinic.visits.datalayer;

import com.petclinic.visits.businesslayer.VisitsService;
import org.graalvm.compiler.lir.VirtualStackSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Transactional(propagation = NOT_SUPPORTED)
public class PersistenceTests {

    @Autowired
    private VisitRepository repo;
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
    public void getVisitsForPet(){
        List<Visit> repoResponse = repo.findByPetId(1);
        assertThat(repoResponse, hasSize(2));
    }

    @Test
    public void Is_deleting_Visit() {
        repo.delete(visit);
        assertFalse(repo.existsById(visit.getId()));
    }

    @Test
    public void Is_Visit_Empty_Dont_Delete(){
        Visit visit = new Visit();
        repo.delete(visit);
        assertFalse(repo.equals(null));
    }

    @Test
    public void Is_Deleting_The_Wrong_Value_Date(){
        Visit visit = new Visit();

    }

    @Test
    public void Is_Deleting_The_Wrong_Value_Description(){

    }

    @Test
    public void Is_Deleting_The_Wrong_Value_PetId(){

    }


}