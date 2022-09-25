package com.petclinic.visits.visitsservicenew.DataLayer;

import com.petclinic.visits.visitsservicenew.BusinessLayer.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Calendar;
import java.util.UUID;

@Service
public class DataSetupService implements CommandLineRunner {


    @Autowired
    private VisitService visitService;

    @Override
    public void run(String... args) throws Exception {

        Calendar date = Calendar.getInstance();


        VisitDTO visit1 = new VisitDTO(UUID.randomUUID().toString(), date.getTime(), "this is a dummy description", 2, 2, true);
        VisitDTO visit2 = new VisitDTO(UUID.randomUUID().toString(), date.getTime(), "this is a visit description", 1, 2, true);

        Flux.just(visit1, visit2).flatMap(x -> visitService.addVisit(Mono.just(x)).log(x.toString())).subscribe();
    }
}
