package com.petclinic.bffapigateway.businesslayer;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VisitsServiceClient;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.dtos.Visits.Status;
import com.petclinic.bffapigateway.dtos.Visits.VisitResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewEligibilityServiceTest {

    private static final String CUSTOMER_ID = "customer-1";

    @Mock
    private CustomersServiceClient customersServiceClient;

    @Mock
    private VisitsServiceClient visitsServiceClient;

    @InjectMocks
    private ReviewEligibilityService eligibilityService;

    private PetResponseDTO pet(String petId) {
        return PetResponseDTO.builder()
                .petId(petId)
                .customerId(CUSTOMER_ID)
                .build();
    }

    private VisitResponseDTO visit(String petId, Status status) {
        return VisitResponseDTO.builder()
                .petId(petId)
                .status(status)
                .build();
    }

    @Test
    void customerWithNoPetsIsIneligible() {
        when(customersServiceClient.getPetsByCustomerId(CUSTOMER_ID))
                .thenReturn(Flux.empty());

        StepVerifier.create(eligibilityService.hasCompletedAppointment(CUSTOMER_ID))
                .expectNext(false)
                .verifyComplete();

        verifyNoInteractions(visitsServiceClient);
    }

    @Test
    void customerWithNoAppointmentsIsIneligible() {
        when(customersServiceClient.getPetsByCustomerId(CUSTOMER_ID))
                .thenReturn(Flux.just(pet("pet-1")));
        when(visitsServiceClient.getVisitsForPet("pet-1"))
                .thenReturn(Flux.empty());

        StepVerifier.create(eligibilityService.hasCompletedAppointment(CUSTOMER_ID))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void scheduledConfirmedAndCancelledAppointmentsDoNotQualify() {
        when(customersServiceClient.getPetsByCustomerId(CUSTOMER_ID))
                .thenReturn(Flux.just(pet("pet-1")));
        when(visitsServiceClient.getVisitsForPet("pet-1"))
                .thenReturn(Flux.just(
                        visit("pet-1", Status.UPCOMING),
                        visit("pet-1", Status.CONFIRMED),
                        visit("pet-1", Status.CANCELLED)));

        StepVerifier.create(eligibilityService.hasCompletedAppointment(CUSTOMER_ID))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void oneCompletedAppointmentMakesCustomerEligible() {
        when(customersServiceClient.getPetsByCustomerId(CUSTOMER_ID))
                .thenReturn(Flux.just(pet("pet-1")));
        when(visitsServiceClient.getVisitsForPet("pet-1"))
                .thenReturn(Flux.just(
                        visit("pet-1", Status.UPCOMING),
                        visit("pet-1", Status.COMPLETED)));

        StepVerifier.create(eligibilityService.hasCompletedAppointment(CUSTOMER_ID))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void completedAppointmentForAnyOfCustomersPetsQualifies() {
        when(customersServiceClient.getPetsByCustomerId(CUSTOMER_ID))
                .thenReturn(Flux.just(pet("pet-1"), pet("pet-2")));
        when(visitsServiceClient.getVisitsForPet("pet-1"))
                .thenReturn(Flux.just(visit("pet-1", Status.CANCELLED)));
        when(visitsServiceClient.getVisitsForPet("pet-2"))
                .thenReturn(Flux.just(visit("pet-2", Status.COMPLETED)));

        StepVerifier.create(eligibilityService.hasCompletedAppointment(CUSTOMER_ID))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void appointmentServiceFailureIsNotTreatedAsEligibility() {
        when(customersServiceClient.getPetsByCustomerId(CUSTOMER_ID))
                .thenReturn(Flux.just(pet("pet-1")));
        when(visitsServiceClient.getVisitsForPet("pet-1"))
                .thenReturn(Flux.error(
                        new IllegalStateException("Visits service unavailable")));

        StepVerifier.create(eligibilityService.hasCompletedAppointment(CUSTOMER_ID))
                .expectErrorMatches(error ->
                        error instanceof IllegalStateException &&
                                error.getMessage().contains("unavailable"))
                .verify();
    }
}
