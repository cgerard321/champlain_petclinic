package com.petclinic.visits.visitsservicenew.Utils;

import com.petclinic.visits.visitsservicenew.DataLayer.Emergency.Emergency;
import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.PetResponseDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.PetsClient;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetsClient;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Emergency.EmergencyRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Emergency.EmergencyResponseDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    private static final String TEST_VET_UUID = UUID.randomUUID().toString();
    private static final String TEST_PET_UUID = UUID.randomUUID().toString();

    private static final Date PET_BIRTHDATE = Date.from(
            LocalDate.of(2023, 2, 21)
                    .atStartOfDay(ZoneId.of("UTC"))
                    .toInstant()
    );

    @Test
    public void testGenerateVisitIdString() {
        String visitId = entityDtoUtil.generateVisitIdString();
        assertNotNull(visitId);
        assertTrue(visitId.matches("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"));
    }

    @Test
    public void testToVisitEntity() {
        VisitRequestDTO requestDTO = new VisitRequestDTO();
        requestDTO.setVisitDate(LocalDateTime.parse("2024-11-25 13:45",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        requestDTO.setDescription("Sample description");

        Visit visit = entityDtoUtil.toVisitEntity(requestDTO);

        assertEquals(requestDTO.getVisitDate(), visit.getVisitDate());
        assertEquals(requestDTO.getDescription(), visit.getDescription());
    }

    @Test
    public void testToEmergencyEntity() {
        EmergencyRequestDTO requestDTO = new EmergencyRequestDTO();
        requestDTO.setVisitDate(LocalDateTime.parse("2024-11-25 13:45",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        requestDTO.setDescription("Sample description");

        Emergency visit = entityDtoUtil.toEmergencyEntity(requestDTO);

        assertEquals(requestDTO.getVisitDate(), visit.getVisitDate());
        assertEquals(requestDTO.getDescription(), visit.getDescription());
    }

    @Test
    public void testGenerateEmergencyIdString() {
        String visitId = entityDtoUtil.generateEmergencyIdString();
        assertNotNull(visitId);
        assertTrue(visitId.matches("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"));
    }

    @Test
    public void testToEmergencyResponseDTO() {
        when(petsClient.getPetById(eq(TEST_PET_UUID)))
                .thenReturn(Mono.just(new PetResponseDTO("ownerId", "petName", PET_BIRTHDATE, "petType", "newPhoto")));
        when(vetsClient.getVetByVetId(eq(TEST_VET_UUID)))
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

        Emergency visit = new Emergency();
        visit.setVisitEmergencyId(UUID.randomUUID().toString());
        visit.setVisitDate(LocalDateTime.parse("2024-11-25 13:45",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        visit.setDescription("Test description");
        visit.setPetId(TEST_PET_UUID);
        visit.setPractitionerId(TEST_VET_UUID);

        Mono<EmergencyResponseDTO> resultMono = entityDtoUtil.toEmergencyResponseDTO(visit);

        StepVerifier.create(resultMono)
                .expectNextMatches(dto ->
                        dto.getVisitEmergencyId().equals(visit.getVisitEmergencyId()) &&
                                "petName".equals(dto.getPetName()) &&
                                PET_BIRTHDATE.equals(dto.getPetBirthDate()) &&
                                "Cristiano".equals(dto.getVetFirstName()) &&
                                "Ronaldo".equals(dto.getVetLastName())
                )
                .verifyComplete();
    }

    @Test
    public void testToVisitResponseDTO() {
        when(petsClient.getPetById(eq(TEST_PET_UUID)))
                .thenReturn(Mono.just(new PetResponseDTO("ownerId", "petName", PET_BIRTHDATE, "petType", "newPhoto")));
        when(vetsClient.getVetByVetId(eq(TEST_VET_UUID)))
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

        Visit visit = new Visit();
        visit.setVisitId(UUID.randomUUID().toString());
        visit.setVisitDate(LocalDateTime.parse("2024-11-25 13:45",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        visit.setDescription("Test description");
        visit.setPetId(TEST_PET_UUID);
        visit.setPractitionerId(TEST_VET_UUID);

        Mono<VisitResponseDTO> resultMono = entityDtoUtil.toVisitResponseDTO(visit);

        StepVerifier.create(resultMono)
                .expectNextMatches(dto ->
                        dto.getVisitId().equals(visit.getVisitId()) &&
                                "petName".equals(dto.getPetName()) &&
                                PET_BIRTHDATE.equals(dto.getPetBirthDate()) &&
                                "Cristiano".equals(dto.getVetFirstName()) &&
                                "Ronaldo".equals(dto.getVetLastName())
                )
                .verifyComplete();
    }
}