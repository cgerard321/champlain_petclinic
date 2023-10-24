package com.petclinic.visits.visitsservicenew.DataLayer;

import com.petclinic.visits.visitsservicenew.DomainClientLayer.BillResponseDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.BillServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Component
@RequiredArgsConstructor
public class DataSetupService implements CommandLineRunner {
    private final VisitRepo visitRepo;
    @Autowired
    private BillServiceClient billServiceClient;

    List<String> billResponseList;

    List<String> customerResponseList;

    @Override
    public void run(String... args) throws Exception {

        Flux<BillResponseDTO> billResponseFlux = billServiceClient.getAllBilling();

        Mono<List<String>> customerResponseListMono = billResponseFlux.flatMap(s -> {
            return Mono.just(s.getCustomerId());
        }).collectList();

        customerResponseList = customerResponseListMono.block();


        Mono<List<String>> billResponseListMono = billResponseFlux.flatMap(s -> {
            return Mono.just(s.getBillId());
        }).collectList();

        billResponseList = billResponseListMono.block();

        Visit visit1 = buildVisit("visitId1", "2022-11-24 13:00", "this is a dummy description", "2", "69f852ca-625b-11ee-8c99-0242ac120002", Status.COMPLETED, billResponseList.get(0), customerResponseList.get(0));
        Visit visit2 = buildVisit("visitId2", "2022-03-01 13:00", "Dog Needs Meds", "1", "69f85766-625b-11ee-8c99-0242ac120002", Status.COMPLETED, billResponseList.get(2), customerResponseList.get(2));
        Visit visit3 = buildVisit("visitId3", "2020-07-19 13:00","Dog Needs Surgery After Meds", "1", "69f85bda-625b-11ee-8c99-0242ac120002", Status.COMPLETED, billResponseList.get(3), customerResponseList.get(3));
        Visit visit4 = buildVisit("visitId4", "2022-12-24 13:00", "Dog Needs Physio-Therapy", "1", "69f85d2e-625b-11ee-8c99-0242ac120002", Status.UPCOMING, billResponseList.get(4), customerResponseList.get(4));
        Visit visit5 = buildVisit("visitId5", "2023-12-24 13:00", "Cat Needs Check-Up", "4", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.UPCOMING, billResponseList.get(3), customerResponseList.get(3));
        Visit visit6 = buildVisit("visitId6", "2023-12-05 15:00", "Animals Needs Operation", "3", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.UPCOMING, billResponseList.get(1), customerResponseList.get(1));
        Visit visit7 = buildVisit("visitId7", "2022-05-20 09:00", "Cat Needs Check-Up", "4", "ac9adeb8-625b-11ee-8c99-0242ac120002", Status.CONFIRMED, billResponseList.get(2), customerResponseList.get(2));


        Flux.just(visit1, visit2, visit3, visit4, visit5, visit6, visit7).flatMap(x -> visitRepo.insert(Mono.just(x)).log(x.toString())).subscribe();

    }

    private Visit buildVisit(String visitId, String visitDate, String description, String petId, String practitionerId, Status status, String billId, String customerId){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime parsedVisitDate = LocalDateTime.parse(visitDate, formatter);

        return Visit.builder()
                .visitId(visitId)
                .visitDate(parsedVisitDate)
                .description(description)
                .petId(petId)
                .practitionerId(practitionerId)
                .status(status)
                .billId(billId)
                .customerId(customerId)
                .build();
    }
}