package com.petclinic.visits.visitsservicenew.BusinessLayer;

import com.petclinic.visits.visitsservicenew.DataLayer.Status;
import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.*;
import com.petclinic.visits.visitsservicenew.Exceptions.BadRequestException;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitResponseDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
@AutoConfigureWebTestClient
class VisitServiceImplTest {

    @Autowired
    private VisitService visitService;

    @MockBean
    private VisitRepo visitRepo;

    @MockBean
    private VetsClient vetsClient;

    @MockBean
    private PetsClient petsClient;


    private final Long dbSize = 2L;

    private final VisitResponseDTO visitResponseDTO = buildVisitResponseDTO();
    private final VisitRequestDTO visitRequestDTO = buildVisitRequestDTO();
    private final String PRAC_ID = visitResponseDTO.getPractitionerId();
    private final String PET_ID = visitResponseDTO.getPetId();
    private final String VISIT_ID = visitResponseDTO.getVisitId();

    String uuidVisit1 = UUID.randomUUID().toString();
    String uuidVisit2 = UUID.randomUUID().toString();
    String uuidVet = UUID.randomUUID().toString();
    String uuidPet = UUID.randomUUID().toString();
    String uuidPhoto = UUID.randomUUID().toString();
    String uuidOwner = UUID.randomUUID().toString();

    Set<SpecialtyDTO> set= new HashSet<>();


    VetDTO vet = VetDTO.builder()
            .vetId(uuidVet)
            .vetBillId("1")
            .firstName("James")
            .lastName("Carter")
            .email("carter.james@email.com")
            .phoneNumber("(514)-634-8276 #2384")
            .imageId("1")
            .resume("Practicing since 3 years")
            .workday("Monday, Tuesday, Friday")
            .active(true)
            .specialties(set)
            .build();

    Date currentDate =new Date();
    PetResponseDTO petResponseDTO = PetResponseDTO.builder()
            .petTypeId(uuidPet)
            .name("Billy")
            .birthDate(currentDate)
            .photoId(uuidPhoto)
            .ownerId(uuidOwner)
            .build();




    Visit visit1 = buildVisit(uuidVisit1,"this is a dummy description",vet.getVetId());
    Visit visit2 = buildVisit(uuidVisit2,"this is a dummy description",vet.getVetId());

    @Test
    void getVisitByVisitId(){
        when(visitRepo.findByVisitId(anyString())).thenReturn(Mono.just(visit1));

        String visitId = visit1.getVisitId();

        Mono<VisitResponseDTO> visitResponseDTOMono = visitService.getVisitByVisitId(visitId);

        StepVerifier
                .create(visitResponseDTOMono)
                .consumeNextWith(foundVisit -> {
                    assertEquals(visit1.getVisitId(), foundVisit.getVisitId());
                    assertEquals(visit1.getVisitDate(), foundVisit.getVisitDate());
                    assertEquals(visit1.getDescription(), foundVisit.getDescription());
                    assertEquals(visit1.getPetId(), foundVisit.getPetId());
                    assertEquals(visit1.getPractitionerId(), foundVisit.getPractitionerId());
                }).verifyComplete();
    }
    @Test
    void getVisitsByPractitionerId(){
        when(visitRepo.findVisitsByPractitionerId(anyString())).thenReturn(Flux.just(visit1));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));
        Flux<VisitResponseDTO> visitResponseDTOFlux = visitService.getVisitsForPractitioner(PRAC_ID);;

        StepVerifier
                .create(visitResponseDTOFlux)
                .consumeNextWith(foundVisit -> {
                    assertEquals(visit1.getVisitId(), foundVisit.getVisitId());
                    assertEquals(visit1.getVisitDate(), foundVisit.getVisitDate());
                    assertEquals(visit1.getDescription(), foundVisit.getDescription());
                    assertEquals(visit1.getPetId(), foundVisit.getPetId());
                    assertEquals(visit1.getPractitionerId(), foundVisit.getPractitionerId());
                }).verifyComplete();
    }

    @Test
    void getVisitsForPet(){
        when(visitRepo.findByPetId(anyString())).thenReturn(Flux.just(visit1));
        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(petResponseDTO));

        Flux<VisitResponseDTO> visitResponseDTOFlux = visitService.getVisitsForPet(PET_ID);

        StepVerifier
                .create(visitResponseDTOFlux)
                .consumeNextWith(foundVisit -> {
                    assertEquals(visit1.getVisitId(), foundVisit.getVisitId());
                    assertEquals(visit1.getVisitDate(), foundVisit.getVisitDate());
                    assertEquals(visit1.getDescription(), foundVisit.getDescription());
                    assertEquals(visit1.getPetId(), foundVisit.getPetId());
                    assertEquals(visit1.getPractitionerId(), foundVisit.getPractitionerId());
                }).verifyComplete();
    }

    @Test
    void getVisitsForStatus(){
        when(visitRepo.findAllByStatus(anyString())).thenReturn(Flux.just(visit1));

        Flux<VisitResponseDTO> visitResponseDTOFlux = visitService.getVisitsForStatus(anyString());

        StepVerifier
                .create(visitResponseDTOFlux)
                .consumeNextWith(foundVisit -> {
                    assertEquals(visit1.getVisitId(), foundVisit.getVisitId());
                    assertEquals(visit1.getVisitDate(), foundVisit.getVisitDate());
                    assertEquals(visit1.getDescription(), foundVisit.getDescription());
                    assertEquals(visit1.getPetId(), foundVisit.getPetId());
                    assertEquals(visit1.getPractitionerId(), foundVisit.getPractitionerId());
                    assertEquals(visit1.getStatus(), foundVisit.getStatus());
                }).verifyComplete();
    }

    /*
    @Test
    void getVisitsByPractitionerIdAndMonth(){
        when(visitRepo.findVisitsByPractitionerIdAndMonth(anyInt(), anyInt())).thenReturn(Flux.just(visit));

        Flux<VisitResponseDTO> visitDTOFlux = visitService.getVisitsByPractitionerIdAndMonth(PET_ID, MONTH);

        StepVerifier
                .create(visitDTOFlux)
                .consumeNextWith(foundVisit -> {
                    assertEquals(visit.getVisitId(), foundVisit.getVisitId());
                    assertEquals(visit.getYear(), foundVisit.getYear());
                    assertEquals(visit.getMonth(), foundVisit.getMonth());
                    assertEquals(visit.getDay(), foundVisit.getDay());
                    assertEquals(visit.getDescription(), foundVisit.getDescription());
                    assertEquals(visit.getPetId(), foundVisit.getPetId());
                    assertEquals(visit.getPractitionerId(), foundVisit.getPractitionerId());
                }).verifyComplete();
    }
     */

    @Test
    void addVisit(){
        when(visitRepo.insert(any(Visit.class))).thenReturn(Mono.just(visit1));
        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(petResponseDTO));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));

        StepVerifier.create(visitService.addVisit(Mono.just(visitRequestDTO)))
                .consumeNextWith(visitDTO1 -> {
                    assertEquals(visit1.getDescription(), visitDTO1.getDescription());
                    assertEquals(visit1.getPetId(), visitDTO1.getPetId());
                    assertEquals(visit1.getVisitDate(), visitDTO1.getVisitDate());
                    assertEquals(visit1.getPractitionerId(), visitDTO1.getPractitionerId());
                }).verifyComplete();
    }

    @Test
    void updateStatusForVisitByVisitId(){
        String status = "CANCELLED";

        when(visitRepo.save(any(Visit.class))).thenReturn(Mono.just(visit1));
        when(visitRepo.findByVisitId(anyString())).thenReturn(Mono.just(visit1));

        StepVerifier.create(visitService.updateStatusForVisitByVisitId(VISIT_ID, status))
                .consumeNextWith(visitDTO1 -> {
                    assertEquals(visit1.getVisitId(), visitDTO1.getVisitId());
                    assertEquals(visit1.getDescription(), visitDTO1.getDescription());
                    assertEquals(visit1.getPetId(), visitDTO1.getPetId());
                    assertEquals(visit1.getVisitDate(), visitDTO1.getVisitDate());
                    assertEquals(visit1.getPractitionerId(), visitDTO1.getPractitionerId());
                    assertEquals(visit1.getStatus(), Status.CANCELLED);
                }).verifyComplete();
    }

    @Test
    void deleteVisitById_visitId_shouldSucceed(){
        //arrange
        String visitId = uuidVisit1.toString();

        Mockito.when(visitRepo.existsByVisitId(visitId)).thenReturn(Mono.just(true));
        Mockito.when(visitRepo.deleteByVisitId(visitId)).thenReturn(Mono.empty());

        //act
        Mono<Void> expectResult = visitService.deleteVisit(visitId);

        //assert
        StepVerifier.create(expectResult)
                .verifyComplete();

        Mockito.verify(visitRepo, Mockito.times(1)).deleteByVisitId(visitId);

    }

    @Test
    void deleteVisitById_visitDoesNotExist_shouldThrowNotFoundException() {
        // Arrange
        String visitId = UUID.randomUUID().toString();

        // Mock the existsByVisitId method to return false, indicating that the visit does not exist
        Mockito.when(visitRepo.existsByVisitId(visitId)).thenReturn(Mono.just(false));

        // Act
        Mono<Void> result = visitService.deleteVisit(visitId);

        // Assert
        StepVerifier.create(result)
                .expectError(NotFoundException.class) // Expecting a NotFoundException
                .verify();

        // Verify that deleteByVisitId was not called
        Mockito.verify(visitRepo, Mockito.never()).deleteByVisitId(visitId);
    }


    @Test
    void updateVisit(){
        when(visitRepo.save(any(Visit.class))).thenReturn(Mono.just(visit1));
        when(visitRepo.findByVisitId(anyString())).thenReturn(Mono.just(visit1));
        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(petResponseDTO));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));

        StepVerifier.create(visitService.updateVisit(VISIT_ID, Mono.just(visitRequestDTO)))
                .consumeNextWith(visitDTO1 -> {
                    assertEquals(visit1.getVisitId(), visitDTO1.getVisitId());
                    assertEquals(visit1.getDescription(), visitDTO1.getDescription());
                    assertEquals(visit1.getPetId(), visitDTO1.getPetId());
                    assertEquals(visit1.getVisitDate(), visitDTO1.getVisitDate());
                    assertEquals(visit1.getPractitionerId(), visitDTO1.getPractitionerId());
                }).verifyComplete();
    }


    private Visit buildVisit(String uuid,String description, String vetId){
        return Visit.builder()
                .visitId(uuid)
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description(description)
                .petId("2")
                .practitionerId(vetId)
                .status(Status.UPCOMING)
                .build();
    }
    private VisitResponseDTO buildVisitResponseDTO(){
        return VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("2")
                .practitionerId(UUID.randomUUID().toString())
                .status(Status.REQUESTED)
                .build();
    }
    private VisitRequestDTO buildVisitRequestDTO() {
            return VisitRequestDTO.builder()
                    .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .description("this is a dummy description")
                    .petId("2")
                    .practitionerId(UUID.randomUUID().toString())
                    .status(Status.REQUESTED)
                    .build();
        }

}
