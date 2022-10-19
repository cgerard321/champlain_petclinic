package com.petclinic.visits.visitsservicenew.DataLayer;

import com.petclinic.visits.visitsservicenew.BusinessLayer.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Service
public class DataSetupService implements CommandLineRunner {


    @Autowired
    private VisitService visitService;

    @Override
    public void run(String... args) throws Exception {

        VisitDTO visit1 = new VisitDTO(UUID.randomUUID().toString(), 2022, 11, 24, "this is a dummy description", 2, 2, true);
        VisitDTO visit2 = new VisitDTO(UUID.randomUUID().toString(), 2022, 3, 1, "Dog Needs Meds", 1, 2, true);
        VisitDTO visit3 = new VisitDTO(UUID.randomUUID().toString(), 2020, 7, 19, "Dog Needs Surgery After Meds", 1, 5, false);
        VisitDTO visit4 = new VisitDTO(UUID.randomUUID().toString(), 2022, 12, 24, "Dog Needs Physio-Therapy", 1, 6, true);


        Flux.just(visit1, visit2, visit3, visit4).flatMap(x -> visitService.addVisit(Mono.just(x)).log(x.toString())).subscribe();
    }
}
