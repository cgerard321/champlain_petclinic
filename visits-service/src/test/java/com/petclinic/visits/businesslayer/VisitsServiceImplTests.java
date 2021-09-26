package com.petclinic.visits.businesslayer;

import com.petclinic.visits.datalayer.Visit;
import com.petclinic.visits.datalayer.VisitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static com.petclinic.visits.datalayer.Visit.visit;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;
import java.util.List;


@SpringBootTest
@ExtendWith(SpringExtension.class)
public class VisitsServiceImplTests {

    @MockBean
    VisitRepository repo;

    @Autowired
    VisitsService visitsService;

//    @Test
//    public void addVisit(){
//        when(repo.save()).thenReturn();
//
//        visitsService.addVisit();
//    }

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
    public void whenValidPetIdThenCreateConfirmedVisitForPet(){
        Visit visit = visit().petId(1).date(new Date()).description("").build();

        when(repo.save(visit)).thenReturn(visit);

        Visit serviceResponse = repo.save(visit);

        assertThat(serviceResponse.getPetId(), equalTo(1));
        assertThat(serviceResponse.isStatus(), equalTo(true));
    }
  
    @Test
    public void whenValidPetIdThenShouldCreateVisitForPet() {
        Visit visit = visit().petId(1).date(new Date()).description("").build();
        
        visitsService.addVisit(visit);
        
        verify(repo, times(1)).save(visit);
    }

    @Test
    public void whenValidPetIdThenCreateCanceledVisitForPet(){
        Visit visit = visit().id(2).petId(1).status(false).build();

        when(repo.save(visit)).thenReturn(visit);

        Visit serviceResponse = repo.save(visit);

        assertThat(serviceResponse.getPetId(), equalTo(1));
        assertThat(serviceResponse.isStatus(), equalTo(false));
    }
}
