package com.petclinic.visits.visitsservicenew.BusinessLayer;

import com.petclinic.visits.visitsservicenew.BusinessLayer.Availability.AvailabilityService;
import com.petclinic.visits.visitsservicenew.BusinessLayer.Prescriptions.PrescriptionService;
import com.petclinic.visits.visitsservicenew.DataLayer.Status;
import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Auth.AuthServiceClient;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.FileService.FilesServiceClient;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Mailing.Mail;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Mailing.MailService;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.PetsClient;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetsClient;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import com.petclinic.visits.visitsservicenew.Utils.EntityDtoUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyScheduleServiceTest {

    @Mock private VisitRepo repo;
    @Mock private VetsClient vetsClient;
    @Mock private PetsClient petsClient;
    @Mock private EntityDtoUtil entityDtoUtil;
    @Mock private AuthServiceClient authServiceClient;
    @Mock private MailService mailService;
    @Mock private PrescriptionService prescriptionService;
    @Mock private FilesServiceClient filesServiceClient;
    @Mock private AvailabilityService availabilityService;

    @InjectMocks private VisitServiceImpl visitService;

    @Test
    void sendsSortedUpcomingAndConfirmedVisitsAndExcludesCompletedVisits() {
        String vetId = "weekly-vet";
        LocalDate monday = LocalDate.of(2027, 1, 4);
        VetDTO vet = VetDTO.builder().vetId(vetId).firstName("Alex").email("alex@example.com").build();
        Visit later = visit(monday.plusDays(3).atTime(15, 0), "Later appointment", Status.UPCOMING);
        Visit completed = visit(monday.plusDays(2).atTime(10, 0), "Completed appointment", Status.COMPLETED);
        Visit confirmed = visit(monday.plusDays(2).atTime(11, 0), "Confirmed appointment", Status.CONFIRMED);
        Visit earlier = visit(monday.plusDays(1).atTime(9, 0), "Earlier appointment", Status.UPCOMING);
        when(vetsClient.getVetByVetId(vetId)).thenReturn(Mono.just(vet));
        when(repo.findByPractitionerIdAndVisitDateBetween(
                vetId, monday.atStartOfDay(), monday.plusDays(7).atStartOfDay()))
                .thenReturn(Flux.just(later, completed, confirmed, earlier));
        when(mailService.sendMail(any(Mail.class))).thenReturn("Message queued");

        StepVerifier.create(visitService.sendSchedule(vetId, monday))
                .expectNext("Message queued")
                .verifyComplete();

        ArgumentCaptor<Mail> mailCaptor = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).sendMail(mailCaptor.capture());
        Mail mail = mailCaptor.getValue();
        assertEquals("alex@example.com", mail.getEmailSendTo());
        assertEquals("Your weekly PetClinic schedule", mail.getEmailTitle());
        assertTrue(mail.getBody().contains("Your schedule for the week starting 2027-01-04"));
        assertTrue(mail.getBody().indexOf("Earlier appointment") < mail.getBody().indexOf("Later appointment"));
        assertTrue(mail.getBody().contains("Confirmed appointment"));
        assertFalse(mail.getBody().contains("Completed appointment"));
    }

    @Test
    void sendsEmptyWeekMessageWhenThereAreNoAppointments() {
        String vetId = "weekly-vet-empty";
        LocalDate monday = LocalDate.of(2027, 1, 4);
        VetDTO vet = VetDTO.builder().vetId(vetId).firstName("Alex").email("alex@example.com").build();
        when(vetsClient.getVetByVetId(vetId)).thenReturn(Mono.just(vet));
        when(repo.findByPractitionerIdAndVisitDateBetween(
                vetId, monday.atStartOfDay(), monday.plusDays(7).atStartOfDay()))
                .thenReturn(Flux.empty());
        when(mailService.sendMail(any(Mail.class))).thenReturn("Message queued");

        StepVerifier.create(visitService.sendSchedule(vetId, monday))
                .expectNext("Message queued")
                .verifyComplete();

        ArgumentCaptor<Mail> mailCaptor = ArgumentCaptor.forClass(Mail.class);
        verify(mailService).sendMail(mailCaptor.capture());
        assertTrue(mailCaptor.getValue().getBody().contains("There are no appointments scheduled for this week."));
    }

    @Test
    void rejectsVetWithoutEmailBeforeQueryingVisits() {
        String vetId = "weekly-vet-no-email";
        when(vetsClient.getVetByVetId(vetId)).thenReturn(Mono.just(VetDTO.builder().email(" ").build()));

        StepVerifier.create(visitService.sendSchedule(vetId, LocalDate.of(2027, 1, 4)))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(repo, never()).findByPractitionerIdAndVisitDateBetween(
                anyString(), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(mailService, never()).sendMail(any(Mail.class));
    }

    @Test
    void rejectsVetWithNullEmailBeforeQueryingVisits() {
        String vetId = "weekly-vet-null-email";
        when(vetsClient.getVetByVetId(vetId)).thenReturn(Mono.just(VetDTO.builder().email(null).build()));

        StepVerifier.create(visitService.sendSchedule(vetId, LocalDate.of(2027, 1, 4)))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(repo, never()).findByPractitionerIdAndVisitDateBetween(
                anyString(), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(mailService, never()).sendMail(any(Mail.class));
    }

    @Test
    void returnsNotFoundWhenVetDoesNotExist() {
        when(vetsClient.getVetByVetId("missing-vet")).thenReturn(Mono.empty());

        StepVerifier.create(visitService.sendSchedule("missing-vet", LocalDate.of(2027, 1, 4)))
                .expectError(NotFoundException.class)
                .verify();

        verifyNoInteractions(repo, mailService);
    }

    @Test
    void automaticScheduleSkipsInactiveVetsAndContinuesAfterVetError() {
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        VetDTO vetWithoutEmail = VetDTO.builder().vetId("vet-no-email").active(true).email("").build();
        VetDTO activeVet = VetDTO.builder().vetId("active-vet").active(true).email("active@example.com").build();
        VetDTO inactiveVet = VetDTO.builder().vetId("inactive-vet").active(false).email("inactive@example.com").build();
        when(vetsClient.getAllVets()).thenReturn(Flux.just(vetWithoutEmail, activeVet, inactiveVet));
        when(vetsClient.getVetByVetId("vet-no-email")).thenReturn(Mono.just(vetWithoutEmail));
        when(vetsClient.getVetByVetId("active-vet")).thenReturn(Mono.just(activeVet));
        when(repo.findByPractitionerIdAndVisitDateBetween(
                "active-vet", monday.atStartOfDay(), monday.plusDays(7).atStartOfDay()))
                .thenReturn(Flux.empty());
        when(mailService.sendMail(any(Mail.class))).thenReturn("Message queued");

        visitService.sendWeeklySchedulesAutomatically();

        verify(repo, timeout(5000)).findByPractitionerIdAndVisitDateBetween(
                "active-vet", monday.atStartOfDay(), monday.plusDays(7).atStartOfDay());
        verify(repo, never()).findByPractitionerIdAndVisitDateBetween(
                eq("vet-no-email"), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(repo, never()).findByPractitionerIdAndVisitDateBetween(
                eq("inactive-vet"), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(mailService, timeout(5000)).sendMail(any(Mail.class));
    }

    private Visit visit(LocalDateTime date, String description, Status status) {
        return Visit.builder().visitDate(date).description(description).status(status).build();
    }
}
