package com.petclinic.vet.servicelayer;

import com.petclinic.vet.businesslayer.vets.VetServiceImpl;
import com.petclinic.vet.dataaccesslayer.badges.BadgeRepository;
import com.petclinic.vet.dataaccesslayer.education.EducationRepository;
import com.petclinic.vet.dataaccesslayer.photos.PhotoRepository;
import com.petclinic.vet.dataaccesslayer.ratings.RatingRepository;
import com.petclinic.vet.dataaccesslayer.vets.Vet;
import com.petclinic.vet.dataaccesslayer.vets.VetRepository;
import com.petclinic.vet.dataaccesslayer.vets.Workday;
import com.petclinic.vet.domainclientlayer.FilesServiceClient;
import com.petclinic.vet.presentationlayer.vets.VetRequestDTO;
import com.petclinic.vet.utils.exceptions.InvalidInputException;
import com.petclinic.vet.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VetProfileUpdateTest {
    private VetRepository repository;
    private VetServiceImpl service;
    private Vet existing;
    private VetRequestDTO request;

    @BeforeEach
    void setUp() {
        repository = mock(VetRepository.class);
        service = new VetServiceImpl(repository, mock(BadgeRepository.class), mock(PhotoRepository.class),
                mock(RatingRepository.class), mock(EducationRepository.class), mock(FilesServiceClient.class));
        existing = Vet.builder().id("mongo-id").vetId("11111111-1111-4111-8111-111111111111")
                .imageId("existing-image").photoId("existing-photo").build();
        request = VetRequestDTO.builder().vetId("body-id-must-not-replace-path-id").vetBillId("bill-id")
                .firstName("Alex").lastName("Morin").email("alex@example.com")
                .phoneNumber("(514)-634-8276 #2384").resume("Veterinarian providing preventive care.")
                .workday(Set.of(Workday.Monday)).workHoursJson("{\"Monday\":[\"Hour_8_9\"]}")
                .specialties(Set.of()).active(true).build();
        when(repository.findVetByVetId(existing.getVetId())).thenReturn(Mono.just(existing));
    }

    @Test
    void updatePreservesPhotoReferencesIdentifiersAndSubmittedSchedule() {
        when(repository.save(any(Vet.class))).thenAnswer(invocation -> {
            Vet saved = invocation.getArgument(0);
            assertEquals("mongo-id", saved.getId());
            assertEquals(existing.getVetId(), saved.getVetId());
            assertEquals("existing-image", saved.getImageId());
            assertEquals("existing-photo", saved.getPhotoId());
            assertEquals(request.getWorkHoursJson(), saved.getWorkHoursJson());
            assertEquals(request.getWorkday(), saved.getWorkday());
            assertEquals("bill-id", saved.getVetBillId());
            assertTrue(saved.isActive());
            return Mono.just(saved);
        });
        StepVerifier.create(service.updateVet(existing.getVetId(), Mono.just(request)))
                .assertNext(response -> {
                    assertEquals("Alex", response.getFirstName());
                    assertEquals(request.getPhoneNumber(), response.getPhoneNumber());
                }).verifyComplete();
        verify(repository).save(any(Vet.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"5145550123", "(514) 555-0123", "+1 514-555-0123", "(514)-634-8276 #2384"})
    void acceptsSupportedPhoneFormats(String phone) {
        request.setPhoneNumber(phone);
        when(repository.save(any(Vet.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        StepVerifier.create(service.updateVet(existing.getVetId(), Mono.just(request)))
                .assertNext(response -> assertEquals(phone, response.getPhoneNumber())).verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = {"firstName", "lastName", "phoneNumber", "email", "resume", "specialties"})
    void rejectsMissingRequiredFieldsWithoutSaving(String field) {
        switch (field) {
            case "firstName" -> request.setFirstName(null);
            case "lastName" -> request.setLastName(null);
            case "phoneNumber" -> request.setPhoneNumber(null);
            case "email" -> request.setEmail(null);
            case "resume" -> request.setResume(null);
            case "specialties" -> request.setSpecialties(null);
        }
        StepVerifier.create(service.updateVet(existing.getVetId(), Mono.just(request)))
                .expectError(InvalidInputException.class).verify();
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"firstName", "lastName", "phoneNumber", "email", "resume"})
    void rejectsInvalidFieldsWithoutSaving(String field) {
        switch (field) {
            case "firstName" -> request.setFirstName(" ");
            case "lastName" -> request.setLastName("a".repeat(31));
            case "phoneNumber" -> request.setPhoneNumber("invalid phone");
            case "email" -> request.setEmail("not-an-email");
            case "resume" -> request.setResume("short");
        }
        StepVerifier.create(service.updateVet(existing.getVetId(), Mono.just(request)))
                .expectError(InvalidInputException.class).verify();
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsUnknownVetWithoutSaving() {
        when(repository.findVetByVetId(existing.getVetId())).thenReturn(Mono.empty());
        StepVerifier.create(service.updateVet(existing.getVetId(), Mono.just(request)))
                .expectError(NotFoundException.class).verify();
        verify(repository, never()).save(any());
    }
}
