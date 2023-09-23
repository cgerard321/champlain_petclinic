package com.petclinic.visits.visitsservicenew.DataLayer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DataSetupService implements CommandLineRunner {
    private final VisitRepo visitRepo;

    @Override
    public void run(String... args) throws Exception {
        Visit visit1 = buildVisit("visitId1", LocalDateTime.parse("2022-11-24T13:00:00"), "this is a dummy description", 2, "2200332", true);
        Visit visit2 = buildVisit("visitId2", LocalDateTime.parse("2022-03-01T13:00:00"), "Dog Needs Meds", 1, "2200332", true);
        Visit visit3 = buildVisit("visitId3", LocalDateTime.parse("2020-07-19T13:00:00"),"Dog Needs Surgery After Meds", 1, "2200332", false);
        Visit visit4 = buildVisit("visitId4", LocalDateTime.parse("2022-12-24T13:00:00"), "Dog Needs Physio-Therapy", 1, "2200332", true);

        Flux.just(visit1, visit2, visit3, visit4).flatMap(x -> visitRepo.insert(Mono.just(x)).log(x.toString())).subscribe();
    }

    private Visit buildVisit(String visitId, LocalDateTime visitDate, String description, int petId, String practitionerId, boolean status){
        return Visit.builder()
                .visitId(visitId)
                .visitDate(visitDate)
                .description(description)
                .petId(petId)
                .practitionerId(practitionerId)
                .status(status)
                .build();
    }
}