package com.petclinic.visits.visitsservicenew.BusinessLayer.Emergency;

import com.petclinic.visits.visitsservicenew.DataLayer.Emergency.Emergency;
import com.petclinic.visits.visitsservicenew.DataLayer.Emergency.EmergencyRepository;
import com.petclinic.visits.visitsservicenew.DataLayer.Emergency.UrgencyLevel;
import com.petclinic.visits.visitsservicenew.DataLayer.Status;
import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.*;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Emergency.EmergencyRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Emergency.EmergencyResponseDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Review.ReviewResponseDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitResponseDTO;
import com.petclinic.visits.visitsservicenew.Utils.EntityDtoUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmergencyServiceUnitTest {

    @InjectMocks
    private EmergencyServiceImpl emergencyService;

    @Mock
    private EmergencyRepository emergencyRepository;

    @Mock
    private EntityDtoUtil entityDtoUtil;

    @Mock
    private VetsClient vetsClient;

    @Mock
    private PetsClient petsClient;


    String uuidVet = UUID.randomUUID().toString();
    String uuidPet = UUID.randomUUID().toString();
    String uuidPhoto = UUID.randomUUID().toString();
    String uuidOwner = UUID.randomUUID().toString();

    Set<SpecialtyDTO> set = new HashSet<>();
    Set<Workday> workdays = new HashSet<>();

    VetDTO vet = VetDTO.builder()
            .vetId(uuidVet)
            .vetBillId("1")
            .firstName("James")
            .lastName("Carter")
            .email("carter.james@email.com")
            .phoneNumber("(514)-634-8276 #2384")
            .imageId("1")
            .resume("Practicing since 3 years")
            .workday(workdays)
            .active(true)
            .specialties(set)
            .build();

    Date currentDate = new Date();
    PetResponseDTO petResponseDTO = PetResponseDTO.builder()
            .petTypeId(uuidPet)
            .name("Billy")
            .birthDate(currentDate)
            .photoId(uuidPhoto)
            .ownerId(uuidOwner)
            .build();




    Emergency emergency2 = Emergency.builder()
            .id(UUID.randomUUID().toString())
            .visitEmergencyId(UUID.randomUUID().toString())
            .visitDate(LocalDateTime.now())
            .description("Critical situation")
            .petId(uuidPet)
            .practitionerId(uuidVet)
            .urgencyLevel(UrgencyLevel.HIGH)
            .emergencyType("death")
            .build();

   // Emergency visit1 = buildEmergency();
  /*  @Test
    void whenGetAllEmergencies_thenReturnAllEmergencies() {
        // Mock the behavior of the repository for the case when a description is provided
        Emergency emergency = new Emergency(); // replace with your actual Visit object


        // Mock the behavior of the repository for the case when no description is provided
        Visit visit2 = new Visit(); // replace with another Visit object
        when(visitRepo.findAll()).thenReturn(Flux.just(emergency));

        // Mock the behavior of entityDtoUtil to map visits to VisitResponseDTO
        VisitResponseDTO visitResponseDTO1 = new VisitResponseDTO(); // replace with your actual VisitResponseDTO object
        VisitResponseDTO visitResponseDTO2 = new VisitResponseDTO(); // another VisitResponseDTO object
        when(entityDtoUtil.toVisitResponseDTO(visit1)).thenReturn(Mono.just(visitResponseDTO1));
        when(entityDtoUtil.toVisitResponseDTO(visit2)).thenReturn(Mono.just(visitResponseDTO2));

        // Test case when description is provided
        Flux<VisitResponseDTO> resultWithDescription = visitService.getAllVisits(description);

        // Verify the results using StepVerifier for the case when description is provided
        StepVerifier.create(resultWithDescription)
                .expectNext(visitResponseDTO1) // Expect the mapped VisitResponseDTO for the filtered visit
                .expectComplete()
                .verify();

        // Test case when no description is provided
        Flux<VisitResponseDTO> resultWithoutDescription = visitService.getAllVisits(null);

        // Verify the results using StepVerifier for the case when no description is provided
        StepVerifier.create(resultWithoutDescription)
                .expectNext(visitResponseDTO2) // Expect the mapped VisitResponseDTO for all visits
                .expectComplete()
                .verify();
    }


    private Emergency buildEmergency (){
        return Emergency.builder()
                .id(UUID.randomUUID().toString())
                .visitEmergencyId(UUID.randomUUID().toString())
                .visitDate(LocalDateTime.now())
                .description("Critical situation")
                .petId(uuidPet)
                .practitionerId(uuidVet)
                .urgencyLevel(UrgencyLevel.HIGH)
                .emergencyType("death")
                .build();
    }
*/
   /* @Test
    public void whenGetEmergencyByVisitEmergencyId_thenReturnEmergency() {
        // Arrange
        when(emergencyRepository.findEmergenciesByVisitEmergencyId(emergency1.getVisitEmergencyId())).thenReturn(Mono.just(emergency1));

        // Act
        Mono<EmergencyResponseDTO> result = emergencyService.GetEmergencyByEmergencyId(emergency1.getVisitEmergencyId());

        // Assert
        StepVerifier
                .create(result)
                .expectNextMatches(emergencyResponseDTO -> emergencyResponseDTO.getVisitEmergencyId().equals(emergency1.getVisitEmergencyId()))
                .verifyComplete();
    }


    @Test
    public void whenAddEmergency_thenReturnEmergencyResponseDTO() {
        // Arrange
        when(emergencyRepository.save(any(Emergency.class))).thenReturn(Mono.just(emergency1));

        EmergencyRequestDTO emergencyRequestDTO = new EmergencyRequestDTO();
        emergencyRequestDTO.setPetName(emergency1.getPetName());
        emergencyRequestDTO.setDescription(emergency1.getDescription());
        emergencyRequestDTO.setUrgencyLevel(emergency1.getUrgencyLevel());
        emergencyRequestDTO.setEmergencyType(emergency1.getEmergencyType());

        // Act
        Mono<EmergencyResponseDTO> result = emergencyService.AddEmergency(Mono.just(emergencyRequestDTO));

        // Assert
        StepVerifier
                .create(result)
                .expectNextMatches(emergencyResponseDTO -> {
                    assertNotNull(emergencyResponseDTO);
                    assertEquals(emergencyResponseDTO.getVisitEmergencyId(), emergency1.getVisitEmergencyId());
                    assertEquals(emergencyResponseDTO.getPetName(), emergency1.getPetName());
                    assertEquals(emergencyResponseDTO.getDescription(), emergency1.getDescription());
                    assertEquals(emergencyResponseDTO.getUrgencyLevel(), emergency1.getUrgencyLevel());
                    assertEquals(emergencyResponseDTO.getEmergencyType(), emergency1.getEmergencyType());
                    return true;
                })
                .verifyComplete();
    }
*/
   @Test
   public void whenUpdateEmergency_thenReturnUpdatedEmergencyResponseDTO() {
       // Arrange
       String existingEmergencyId = emergency2.getVisitEmergencyId();

       EmergencyRequestDTO updatedEmergencyRequestDTO = new EmergencyRequestDTO();
       updatedEmergencyRequestDTO.setVisitDate(LocalDateTime.now().plusDays(1));
       updatedEmergencyRequestDTO.setDescription("Updated situation");
       updatedEmergencyRequestDTO.setPetId(uuidPet);
       updatedEmergencyRequestDTO.setPractitionerId(uuidVet);
       updatedEmergencyRequestDTO.setUrgencyLevel(UrgencyLevel.MEDIUM);
       updatedEmergencyRequestDTO.setEmergencyType("Updated Type");

       when(emergencyRepository.findEmergenciesByVisitEmergencyId(existingEmergencyId))
               .thenReturn(Mono.just(emergency2));

       when(petsClient.getPetById(uuidPet)).thenReturn(Mono.just(petResponseDTO));
       when(vetsClient.getVetByVetId(uuidVet)).thenReturn(Mono.just(vet));

       Emergency mappedFromReq = Emergency.builder()
               .visitDate(updatedEmergencyRequestDTO.getVisitDate())
               .description(updatedEmergencyRequestDTO.getDescription())
               .petId(updatedEmergencyRequestDTO.getPetId())
               .practitionerId(updatedEmergencyRequestDTO.getPractitionerId())
               .urgencyLevel(updatedEmergencyRequestDTO.getUrgencyLevel())
               .emergencyType(updatedEmergencyRequestDTO.getEmergencyType())
               .build();
       when(entityDtoUtil.toEmergencyEntity(updatedEmergencyRequestDTO))
               .thenReturn(mappedFromReq);

       Emergency updatedEmergency = Emergency.builder()
               .id(emergency2.getId())
               .visitEmergencyId(existingEmergencyId)
               .visitDate(updatedEmergencyRequestDTO.getVisitDate())
               .description(updatedEmergencyRequestDTO.getDescription())
               .petId(updatedEmergencyRequestDTO.getPetId())
               .practitionerId(updatedEmergencyRequestDTO.getPractitionerId())
               .urgencyLevel(updatedEmergencyRequestDTO.getUrgencyLevel())
               .emergencyType(updatedEmergencyRequestDTO.getEmergencyType())
               .build();
       when(emergencyRepository.save(any(Emergency.class)))
               .thenReturn(Mono.just(updatedEmergency));

       EmergencyResponseDTO expectedResponse = EmergencyResponseDTO.builder()
               .visitEmergencyId(updatedEmergency.getVisitEmergencyId())
               .visitDate(updatedEmergency.getVisitDate())
               .description(updatedEmergency.getDescription())
               .petId(updatedEmergency.getPetId())
               .practitionerId(updatedEmergency.getPractitionerId())
               .urgencyLevel(updatedEmergency.getUrgencyLevel())
               .emergencyType(updatedEmergency.getEmergencyType())
               .build();
       when(entityDtoUtil.toEmergencyResponseDTO(updatedEmergency))
               .thenReturn(Mono.just(expectedResponse));

       // Act
       Mono<EmergencyResponseDTO> result =
               emergencyService.updateEmergency(existingEmergencyId, Mono.just(updatedEmergencyRequestDTO));

       // Assert
       StepVerifier.create(result)
               .expectNextMatches(emergencyResponseDTO -> {
                   assertNotNull(emergencyResponseDTO);
                   assertEquals(updatedEmergency.getVisitEmergencyId(), emergencyResponseDTO.getVisitEmergencyId());
                   assertEquals(updatedEmergency.getDescription(), emergencyResponseDTO.getDescription());
                   assertEquals(updatedEmergency.getUrgencyLevel(), emergencyResponseDTO.getUrgencyLevel());
                   assertEquals(updatedEmergency.getEmergencyType(), emergencyResponseDTO.getEmergencyType());
                   assertEquals(updatedEmergency.getPetId(), emergencyResponseDTO.getPetId());
                   assertEquals(updatedEmergency.getPractitionerId(), emergencyResponseDTO.getPractitionerId());
                   return true;
               })
               .verifyComplete();
   }

    @Test
    void whenUpdateEmergency_withInvalidPetId_thenThrowsNotFoundException() {
        // Arrange
        String visitEmergencyId = emergency2.getVisitEmergencyId();
        String invalidPetId = "bad-pet-id";

        EmergencyRequestDTO req = new EmergencyRequestDTO();
        req.setVisitDate(LocalDateTime.now().plusDays(2));
        req.setDescription("Should not update");
        req.setPetId(invalidPetId);
        req.setPractitionerId(uuidVet);
        req.setUrgencyLevel(UrgencyLevel.HIGH);
        req.setEmergencyType("injury");

        when(emergencyRepository.findEmergenciesByVisitEmergencyId(visitEmergencyId))
                .thenReturn(Mono.just(emergency2));

        when(petsClient.getPetById(invalidPetId)).thenReturn(Mono.empty());

        when(vetsClient.getVetByVetId(uuidVet)).thenReturn(Mono.just(vet));

        // Act
        Mono<EmergencyResponseDTO> result =
                emergencyService.updateEmergency(visitEmergencyId, Mono.just(req));

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(ex ->
                        ex instanceof NotFoundException &&
                                ex.getMessage().equals("No pet was found with petId: " + invalidPetId)
                )
                .verify();

        verify(entityDtoUtil, never()).toEmergencyEntity(any());
        verify(emergencyRepository, never()).save(any());
    }



    @Test
   public void whenDeleteEmergencyByVisitEmergencyId_thenDeleteEmergency() {
       // Arrange
       when(emergencyRepository.findEmergenciesByVisitEmergencyId(emergency2.getVisitEmergencyId()))
               .thenReturn(Mono.just(emergency2));
       when(emergencyRepository.delete(emergency2))
               .thenReturn(Mono.empty());
       when(entityDtoUtil.toEmergencyResponseDTO(emergency2))
               .thenReturn(Mono.just(EmergencyResponseDTO.builder()
                       .visitEmergencyId(emergency2.getVisitEmergencyId())
                       .description(emergency2.getDescription())
                       .urgencyLevel(emergency2.getUrgencyLevel())
                       .emergencyType(emergency2.getEmergencyType())
                       .petId(emergency2.getPetId())
                       .practitionerId(emergency2.getPractitionerId())
                       .visitDate(emergency2.getVisitDate())
                       .build()));

       // Act
       Mono<EmergencyResponseDTO> result = emergencyService.DeleteEmergency(emergency2.getVisitEmergencyId());

       // Assert
       StepVerifier.create(result)
               .expectNextMatches(emergencyResponseDTO ->
                       emergencyResponseDTO.getVisitEmergencyId().equals(emergency2.getVisitEmergencyId()))
               .verifyComplete();
   }


    @Test
    public void whenEmergencyIdDoesNotExistOnDelete_thenReturnNotFoundException() {
        // Arrange
        String nonExistentEmergencyId = UUID.randomUUID().toString();
        when(emergencyRepository.findEmergenciesByVisitEmergencyId(nonExistentEmergencyId))
                .thenReturn(Mono.empty());

        // Act
        Mono<EmergencyResponseDTO> result = emergencyService.DeleteEmergency(nonExistentEmergencyId);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof NotFoundException &&
                                throwable.getMessage().equals("emergency id is not found: " + nonExistentEmergencyId)
                )
                .verify();
    }







}