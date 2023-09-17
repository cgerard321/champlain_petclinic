package com.petclinic.visits.visitsservicenew.DataLayer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DataSetupService implements CommandLineRunner {
    private final VisitRepo visitRepo;

    @Override
    public void run(String... args) throws Exception {
        Visit visit1 = buildVisit("visitId1", 2022, 11, 24, "this is a dummy description", 2, 2, true);
        Visit visit2 = buildVisit("visitId2", 2022, 3, 1, "Dog Needs Meds", 1, 2, true);
        Visit visit3 = buildVisit("visitId3", 2020, 7, 19, "Dog Needs Surgery After Meds", 1, 5, false);
        Visit visit4 = buildVisit("visitId4", 2022, 12, 24, "Dog Needs Physio-Therapy", 1, 6, true);

        Flux.just(visit1, visit2, visit3, visit4).flatMap(x -> visitRepo.insert(Mono.just(x)).log(x.toString())).subscribe();
    }

    private Visit buildVisit(String visitId, int year, int month, int day, String description, int petId, int practitionerId, boolean status){
        return Visit.builder()
                .visitId(visitId)
                .year(year)
                .month(month)
                .day(day)
                .description(description)
                .petId(petId)
                .practitionerId(practitionerId)
                .status(status)
                .build();
    }
}
