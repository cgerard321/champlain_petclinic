package com.petclinic.visits.visitsservicenew.Utils;

import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.PetResponseDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.PetsClient;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetsClient;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@SpringBootTest
public class EntityDtoUtilTest {

    @Autowired
    private EntityDtoUtil entityDtoUtil;

    @MockBean
    private VetsClient vetsClient;

    @MockBean
    private PetsClient petsClient;


    String testVetUUID = UUID.randomUUID().toString();
    String testPetUUID = UUID.randomUUID().toString();

    @Test
    public void testToVisitEntity() {

        VisitRequestDTO requestDTO = new VisitRequestDTO();
        requestDTO.setVisitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        requestDTO.setDescription("Sample description");

        Visit visit = entityDtoUtil.toVisitEntity(requestDTO);

        assertEquals(requestDTO.getVisitDate(), visit.getVisitDate());
        assertEquals(requestDTO.getDescription(), visit.getDescription());
    }


    @Test
    public void testToVisitResponseDTO() {
        // Mock responses for petsClient and vetsClient
        when(petsClient.getPetById(eq(testPetUUID)))
                .thenReturn(Mono.just(new PetResponseDTO("ownerId", "petName", new Date(2023, 2, 21), "petType", "newPhoto")));
        when(vetsClient.getVetByVetId(eq(testVetUUID)))
                .thenReturn(Mono.just(
                        VetDTO.builder()
                                .vetId("vetId")
                                .vetBillId("billId")
                                .firstName("Cristiano")
                                .lastName("Ronaldo")
                                .email("cr7@gmail.com")
                                .phoneNumber("5149950205")
                                .imageId("image123")
                                .resume("Resume")
                                .workday(new HashSet<>())
                                .active(true)
                                .specialties(new HashSet<>())
                                .build()
                ));

        // Create visit
        Visit visit = new Visit();
        visit.setVisitId(UUID.randomUUID().toString());
        visit.setVisitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        visit.setDescription("Test description");
        visit.setPetId(testPetUUID); // passing pre-defined testPetUUID
        visit.setPractitionerId(testVetUUID); // passing pre-defined testVetUUID

        // Call the toVisitResponseDTO method
        Mono<VisitResponseDTO> resultMono = entityDtoUtil.toVisitResponseDTO(visit);

        // Use Step verifier to ensure matching responses
        StepVerifier.create(resultMono)
                .expectNextMatches(dto -> {
                    return dto.getVisitId().equals(visit.getVisitId()) &&
                            dto.getPetName().equals("petName") &&
                            dto.getPetBirthDate().equals(new Date(2023, 2, 21)) &&
                            dto.getVetFirstName().equals("Cristiano") &&
                            dto.getVetLastName().equals("Ronaldo");
                })
                .verifyComplete();
    }
}