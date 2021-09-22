package com.petclinic.visits.businesslayer;

import com.petclinic.visits.datalayer.VisitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.mockito.Mockito.when;

import org.springframework.test.context.junit.jupiter.SpringExtension;


@SpringBootTest
@ExtendWith(SpringExtension.class)
public class VisitsServiceImplTests {

    @MockBean
    VisitRepository repo;

    @Autowired
    VisitsService visitsService;

    @Test
    public void addVisit(){
        when(repo.save()).thenReturn();

        visitsService.addVisit();
    }

}
