package com.petclinic.visits.visitsservicenew.BusinessLayer.Emergency;

import com.petclinic.visits.visitsservicenew.DataLayer.Emergency.Emergency;
import com.petclinic.visits.visitsservicenew.DataLayer.Emergency.EmergencyRepository;
import com.petclinic.visits.visitsservicenew.DataLayer.Emergency.UrgencyLevel;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Emergency.EmergencyRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Emergency.EmergencyResponseDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Review.ReviewResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmergencyServiceUnitTest {

    @InjectMocks
    private EmergencyServiceImpl emergencyService;

    @Mock
    private EmergencyRepository emergencyRepository;

    Emergency emergency1 = Emergency.builder()
            .id(UUID.randomUUID().toString())
            .visitEmergencyId(UUID.randomUUID().toString())
            .visitDate(LocalDateTime.now())
            .description("Critical situation")
            .petName("Bobby")
            .urgencyLevel(UrgencyLevel.HIGH)
            .emergencyType("death")
            .build();

    Emergency emergency2 = Emergency.builder()
            .id(UUID.randomUUID().toString())
            .visitEmergencyId(UUID.randomUUID().toString())
            .visitDate(LocalDateTime.now())
            .description("Critical situation")
            .petName("hamidus")
            .urgencyLevel(UrgencyLevel.HIGH)
            .emergencyType("death")
            .build();
    @Test
    public void whenGetAllEmergencies_thenReturnEmergencies() {
        // Arrange
        when(emergencyRepository.findAll()).thenReturn(Flux.just(emergency1, emergency2));

        // Act
        Flux<EmergencyResponseDTO> result = emergencyService.GetAllEmergencies();

        // Assert
        StepVerifier
                .create(result)
                .expectNextMatches(emergencyResponseDTO -> emergencyResponseDTO.getVisitEmergencyId().equals(emergency1.getVisitEmergencyId()))
                .expectNextMatches(emergencyResponseDTO -> emergencyResponseDTO.getVisitEmergencyId().equals(emergency2.getVisitEmergencyId()))
                .verifyComplete();
    }

    @Test
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

    @Test
    public void whenUpdateEmergency_thenReturnUpdatedEmergencyResponseDTO() {
        // Arrange
        String existingEmergencyId = emergency1.getVisitEmergencyId();
        Emergency updatedEmergency = Emergency.builder()
                .id(emergency1.getId())
                .visitEmergencyId(existingEmergencyId)
                .visitDate(emergency1.getVisitDate())
                .description("Updated situation") // Updated description
                .petName("Updated Pet Name") // Updated pet name
                .urgencyLevel(UrgencyLevel.MEDIUM) // Updated urgency level// Updated critical status
                .emergencyType("Updated Type") // Updated emergency type
                .build();

        EmergencyRequestDTO updatedEmergencyRequestDTO = new EmergencyRequestDTO();
        updatedEmergencyRequestDTO.setDescription(updatedEmergency.getDescription());
        updatedEmergencyRequestDTO.setPetName(updatedEmergency.getPetName());
        updatedEmergencyRequestDTO.setUrgencyLevel(updatedEmergency.getUrgencyLevel());
        updatedEmergencyRequestDTO.setEmergencyType(updatedEmergency.getEmergencyType());

        when(emergencyRepository.findEmergenciesByVisitEmergencyId(existingEmergencyId)).thenReturn(Mono.just(emergency1));
        when(emergencyRepository.save(any(Emergency.class))).thenReturn(Mono.just(updatedEmergency));

        // Act
        Mono<EmergencyResponseDTO> result = emergencyService.UpdateEmergency(Mono.just(updatedEmergencyRequestDTO), existingEmergencyId);

        // Assert
        StepVerifier
                .create(result)
                .expectNextMatches(emergencyResponseDTO -> {
                    assertNotNull(emergencyResponseDTO);
                    assertEquals(updatedEmergency.getVisitEmergencyId(), emergencyResponseDTO.getVisitEmergencyId());
                    assertEquals(updatedEmergency.getDescription(), emergencyResponseDTO.getDescription());
                    assertEquals(updatedEmergency.getPetName(), emergencyResponseDTO.getPetName());
                    assertEquals(updatedEmergency.getUrgencyLevel(), emergencyResponseDTO.getUrgencyLevel());
                    assertEquals(updatedEmergency.getEmergencyType(), emergencyResponseDTO.getEmergencyType());
                    return true;
                })
                .verifyComplete();
    }


    @Test
    public void whenDeleteEmergencyByVisitEmergencyId_thenDeleteEmergency() {
        // Arrange
        when(emergencyRepository.findEmergenciesByVisitEmergencyId(emergency1.getVisitEmergencyId())).thenReturn(Mono.just(emergency1));
        when(emergencyRepository.delete(emergency1)).thenReturn(Mono.empty());

        // Act
        Mono<EmergencyResponseDTO> result = emergencyService.DeleteEmergency(emergency1.getVisitEmergencyId());

        // Assert
        StepVerifier
                .create(result)
                .expectNextMatches(emergencyResponseDTO -> emergencyResponseDTO.getVisitEmergencyId().equals(emergency1.getVisitEmergencyId()))
                .verifyComplete();
    }


    @Test
    public void whenEmergencyIdDoesNotExistOnDelete_thenReturnNotFoundException() {
        // Arrange
        String nonExistentEmergencyId = UUID.randomUUID().toString();
        when(emergencyRepository.findEmergenciesByVisitEmergencyId(nonExistentEmergencyId)).thenReturn(Mono.empty());

        // Act
        Mono<EmergencyResponseDTO> result = emergencyService.DeleteEmergency(nonExistentEmergencyId);

        // Assert
        StepVerifier
                .create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof NotFoundException &&
                                throwable.getMessage().equals("emergency id is not found: " + nonExistentEmergencyId)
                )
                .verify();
    }








}