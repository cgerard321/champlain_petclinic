package com.petclinic.visits.datalayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.petclinic.visits.datalayer.Visit.visit;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Transactional(propagation = NOT_SUPPORTED)
public class PersistenceTests {

    @Autowired
    private VisitRepository repo;

    @BeforeEach
    public void setupDb(){
        repo.deleteAll();

        // add setup data here
        Visit visit = Visit.visit()
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
    public void confirmAndCancelAppointment(){
        List<Visit> repoResponse = repo.findByPetId(1);

        repoResponse.get(0).setStatus(true);
        repoResponse.get(1).setStatus(false);

        assertThat(repoResponse, hasSize(2));
        assertThat(repoResponse.get(0).isStatus(), equalTo(true));
        assertThat(repoResponse.get(1).isStatus(), equalTo(false));
    }

    @Test
    public void createVisitForPet() {
        Visit visit = visit().petId(3).date(new Date()).description("").build();
        repo.save(visit);
        
        List<Visit> repoResponse = repo.findByPetId(3);
        
        assertThat(repoResponse, hasSize(1));
    }

  
  
    @Test
    public void getVisitsForNonExistentPet(){
        List<Visit> repoResponse = repo.findByPetId(0);


        assertThat(repoResponse, hasSize(0));

    }

}
