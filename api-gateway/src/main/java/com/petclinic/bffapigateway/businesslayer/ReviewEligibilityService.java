package com.petclinic.bffapigateway.businesslayer;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VisitsServiceClient;
import com.petclinic.bffapigateway.dtos.Visits.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReviewEligibilityService {
    private final CustomersServiceClient customersServiceClient;
    private final VisitsServiceClient visitsServiceClient;

    public Mono<Boolean> hasCompletedAppointment(String customerId) {
        return customersServiceClient.getPetsByCustomerId(customerId)
                .flatMap(pet ->
                        visitsServiceClient.getVisitsForPet(pet.getPetId()))
                .any(visit ->
                        visit.getStatus() == Status.COMPLETED || visit.getStatus() == Status.ARCHIVED);
    }
}
