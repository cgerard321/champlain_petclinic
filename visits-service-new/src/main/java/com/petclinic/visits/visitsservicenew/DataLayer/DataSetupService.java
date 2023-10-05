package com.petclinic.visits.visitsservicenew.DataLayer;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Service
@RequiredArgsConstructor
public class DataSetupService implements CommandLineRunner {
    private final VisitRepo visitRepo;
    @Override
    public void run(String... args) throws Exception {

        Visit visit1 = buildVisit("visitId1", "2022-11-24 13:00", "this is a dummy description", "2", "69f852ca-625b-11ee-8c99-0242ac120002", Status.COMPLETED);
        Visit visit2 = buildVisit("visitId2", "2022-03-01 13:00", "Dog Needs Meds", "1", "69f85766-625b-11ee-8c99-0242ac120002", Status.COMPLETED);
        Visit visit3 = buildVisit("visitId3", "2020-07-19 13:00","Dog Needs Surgery After Meds", "1", "69f85bda-625b-11ee-8c99-0242ac120002", Status.COMPLETED);
        Visit visit4 = buildVisit("visitId4", "2022-12-24 13:00", "Dog Needs Physio-Therapy", "1", "69f85d2e-625b-11ee-8c99-0242ac120002", Status.UPCOMING);
        Visit visit5 = buildVisit("visitId5", "2023-12-24 13:00", "Cat Needs Check-Up", "4", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.UPCOMING);

        Flux.just(visit1, visit2, visit3, visit4, visit5).flatMap(x -> visitRepo.insert(Mono.just(x)).log(x.toString())).subscribe();
    }

    private Visit buildVisit(String visitId, String visitDate, String description, String petId, String practitionerId, Status status){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime parsedVisitDate = LocalDateTime.parse(visitDate, formatter);

        return Visit.builder()
                .visitId(visitId)
                .visitDate(parsedVisitDate)
                .description(description)
                .petId(petId)
                .practitionerId(practitionerId)
                .status(status)
                .build();
    }

}