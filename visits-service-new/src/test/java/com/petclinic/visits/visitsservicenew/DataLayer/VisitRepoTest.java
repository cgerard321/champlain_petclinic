package com.petclinic.visits.visitsservicenew.DataLayer;

import com.petclinic.visits.visitsservicenew.BusinessLayer.VisitService;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.Flow;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@DataMongoTest
public class VisitRepoTest {

    @Autowired
    private VisitRepo visitRepo;


    @Test
    public void shouldSaveOneVisit(){

        Publisher<Visit> setup = visitRepo.deleteAll().thenMany(visitRepo.save(buildVisit()));

        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();
    }

    private Visit buildVisit(){

        return Visit.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .year(2022)
                .month(11)
                .day(24)
                .description("this is a dummy description")
                .petId(2)
                .practitionerId(2)
                .status(true).build();
    }

}
