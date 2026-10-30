package com.petclinic.vet.servicelayer;

import com.petclinic.vet.businesslayer.vets.VetServiceImpl;
import com.petclinic.vet.dataaccesslayer.badges.Badge;
import com.petclinic.vet.dataaccesslayer.badges.BadgeRepository;
import com.petclinic.vet.dataaccesslayer.education.EducationRepository;
import com.petclinic.vet.dataaccesslayer.photos.PhotoRepository;
import com.petclinic.vet.dataaccesslayer.ratings.RatingRepository;
import com.petclinic.vet.dataaccesslayer.vets.Vet;
import com.petclinic.vet.dataaccesslayer.vets.VetRepository;
import com.petclinic.vet.domainclientlayer.FilesServiceClient;
import com.petclinic.vet.presentationlayer.files.FileResponseDTO;
import com.petclinic.vet.presentationlayer.vets.SpecialtyDTO;
import com.petclinic.vet.presentationlayer.vets.VetRequestDTO;
import com.petclinic.vet.utils.exceptions.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VetCreationTest {
    private VetRepository repository;
    private BadgeRepository badges;
    private FilesServiceClient files;
    private VetServiceImpl service;
    private VetRequestDTO request;

    @BeforeEach
    void setUp() {
        repository = mock(VetRepository.class);
        badges = mock(BadgeRepository.class);
        files = mock(FilesServiceClient.class);
        service = new VetServiceImpl(repository, badges, mock(PhotoRepository.class),
                mock(RatingRepository.class), mock(EducationRepository.class), files);
        request = VetRequestDTO.builder().vetId("new-vet").firstName("Alex").lastName("Morin")
                .email("alex@example.com").phoneNumber("514-555-0123").resume("Preventive animal care.")
                .specialties(Set.of(SpecialtyDTO.builder().specialtyId("").name("surgery").build()))
                .workday(Set.of()).active(true).build();
    }
    private void allowPersistence() {
        when(repository.save(any(Vet.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(badges.save(any(Badge.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
    }
    @Test
    void createsWithoutPhotoAndGeneratesSpecialtyIdentifiers() {
        allowPersistence();
        StepVerifier.create(service.addVet(Mono.just(request))).assertNext(vet -> {
            assertEquals("new-vet", vet.getVetId());
            assertEquals("514-555-0123", vet.getPhoneNumber());
            assertNull(vet.getPhoto());
            assertFalse(vet.getSpecialties().iterator().next().getSpecialtyId().isBlank());
        }).verifyComplete();
        verify(repository).save(any(Vet.class));
        verifyNoInteractions(files);
    }
    @Test
    void createsWithDefaultPhoto() {
        allowPersistence(); request.setPhotoDefault(true);
        FileResponseDTO photo = FileResponseDTO.builder().fileId("photo-id").build();
        when(files.addFile(any())).thenReturn(Mono.just(photo));
        StepVerifier.create(service.addVet(Mono.just(request))).assertNext(vet ->
                assertEquals("photo-id", vet.getPhoto().getFileId())).verifyComplete();
        verify(repository).save(argThat(vet -> "photo-id".equals(vet.getImageId())));
    }
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"123", "abcdefghijklmnopqrst"})
    void rejectsInvalidPhoneBeforePersistence(String phone) {
        request.setPhoneNumber(phone);
        StepVerifier.create(service.addVet(Mono.just(request))).expectError(InvalidInputException.class).verify();
        verifyNoInteractions(repository, badges, files);
    }
    @Test
    void rejectsMissingNameBeforePersistence() {
        request.setFirstName(null);
        StepVerifier.create(service.addVet(Mono.just(request))).expectError(InvalidInputException.class).verify();
        verifyNoInteractions(repository, badges, files);
    }
    @Test
    void propagatesPhotoFailureWithoutSavingVet() {
        request.setPhotoDefault(true);
        when(files.addFile(any())).thenReturn(Mono.error(new IllegalStateException("File service unavailable")));
        StepVerifier.create(service.addVet(Mono.just(request))).expectError(IllegalStateException.class).verify();
        verifyNoInteractions(repository, badges);
    }
}
