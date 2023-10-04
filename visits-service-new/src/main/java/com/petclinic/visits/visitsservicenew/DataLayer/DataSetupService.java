package com.petclinic.visits.visitsservicenew.DataLayer;

import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetsClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DataSetupService implements CommandLineRunner {
    private final VisitRepo visitRepo;
    private final VetsClient vetsClient;

    private static final int MAX_RETRIES = 5;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(20);

    @Override
    public void run(String... args) throws Exception {

        List<String> vetIds = getVetIds();

        Visit visit1 = buildVisit("visitId1", "2022-11-24 13:00", "this is a dummy description", "2", vetIds.get(0), Status.COMPLETED);
        Visit visit2 = buildVisit("visitId2", "2022-03-01 13:00", "Dog Needs Meds", "1", vetIds.get(1), Status.COMPLETED);
        Visit visit3 = buildVisit("visitId3", "2020-07-19 13:00","Dog Needs Surgery After Meds", "1", vetIds.get(2), Status.COMPLETED);
        Visit visit4 = buildVisit("visitId4", "2022-12-24 13:00", "Dog Needs Physio-Therapy", "1", vetIds.get(3), Status.UPCOMING);
        Visit visit5 = buildVisit("visitId5", "2023-12-24 13:00", "Cat Needs Check-Up", "4", vetIds.get(4), Status.UPCOMING);

        Flux.just(visit1, visit2, visit3, visit4, visit5).flatMap(x -> visitRepo.insert(Mono.just(x)).log(x.toString())).subscribe();
    }

    private List<String> getVetIds() {
       return vetsClient.getAllVets()
                .map(VetDTO::getVetId)
                .collectList()
                .retryWhen(Retry.fixedDelay(MAX_RETRIES, RETRY_DELAY))
                .blockOptional()
                .orElseThrow(() -> new IllegalStateException("Failed to retrieve vetIds."));
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