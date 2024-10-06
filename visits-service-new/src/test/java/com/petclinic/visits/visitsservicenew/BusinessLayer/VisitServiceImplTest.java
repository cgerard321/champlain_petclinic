package com.petclinic.visits.visitsservicenew.BusinessLayer;

import com.petclinic.visits.visitsservicenew.DataLayer.Status;
import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitRepo;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Mailing.Mail;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.Mailing.MailService;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.*;
import com.petclinic.visits.visitsservicenew.Exceptions.BadRequestException;
import com.petclinic.visits.visitsservicenew.Exceptions.DuplicateTimeException;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitResponseDTO;
import com.petclinic.visits.visitsservicenew.Utils.EntityDtoUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


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
    @MockBean
    private MailService mailService;

    @MockBean
    private EntityDtoUtil entityDtoUtil;

//    private final Long dbSize = 2L;

    private final VisitResponseDTO visitResponseDTO = buildVisitResponseDTO();
    private final VisitRequestDTO visitRequestDTO = buildVisitRequestDTO();
    private final String PRAC_ID = visitResponseDTO.getPractitionerId();
//    private final String PET_ID = visitResponseDTO.getPetId();
//    private final String VISIT_ID = visitResponseDTO.getVisitId();


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


    Visit visit1 = buildVisit("this is a dummy description");
//    Visit visit2 = buildVisit("this is a dummy description");


    @Test
    void getAllVisits() {
        // Mock the behavior of the repository for the case when a description is provided
        Visit visit1 = new Visit(); // replace with your actual Visit object
        String description = "checkup";

        when(visitRepo.findVisitsByDescriptionContainingIgnoreCase(description)).thenReturn(Flux.just(visit1));

        // Mock the behavior of the repository for the case when no description is provided
        Visit visit2 = new Visit(); // replace with another Visit object
        when(visitRepo.findAll()).thenReturn(Flux.just(visit2));

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


    @Test
    void getVisitByVisitId() {
        when(visitRepo.findByVisitId(anyString())).thenReturn(Mono.just(visit1));
        when(entityDtoUtil.toVisitResponseDTO(any())).thenReturn(Mono.just(visitResponseDTO));


        StepVerifier.create(visitService.getVisitByVisitId(visitResponseDTO.getVisitId()))
                .expectNextMatches(visitDTO -> visitDTO.getVisitId().equals(visit1.getVisitId()))
                .expectComplete()
                .verify();
    }


    @Test
    void getVisitsByPractitionerId() {
        when(visitRepo.findVisitsByPractitionerId(anyString())).thenReturn(Flux.just(visit1));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));
        when(entityDtoUtil.toVisitResponseDTO(any())).thenReturn(Mono.just(visitResponseDTO));
        Flux<VisitResponseDTO> visitResponseDTOFlux = visitService.getVisitsForPractitioner(PRAC_ID);
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));
        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(petResponseDTO));
        when(entityDtoUtil.toVisitResponseDTO(any())).thenReturn(Mono.just(visitResponseDTO));
        // Mock the response from your repository (assuming you have a valid visit)
        when(visitRepo.findVisitsByPractitionerId(vet.getVetId()))
                .thenReturn(Flux.just(visit1));

        // Execute the method under test
        StepVerifier.create(visitService.getVisitsForPractitioner(vet.getVetId()))
                .expectNextMatches(visitDTO -> visitDTO.getVisitId().equals(visit1.getVisitId()))
                .expectComplete()
                .verify();
    }

    @Test
    public void getVisitsForPet () {
        // Arrange
        String petId = "yourPetId";

        Visit visit1 = buildVisit("Visit Description");

        VisitResponseDTO visitResponseDTO = buildVisitResponseDTO();

        // Mock the behavior of dependencies
        when(visitRepo.findByPetId(petId)).thenReturn(Flux.just(visit1));
        when(entityDtoUtil.toVisitResponseDTO(visit1)).thenReturn(Mono.just(visitResponseDTO));
        when(petsClient.getPetById(petId)).thenReturn(Mono.just(petResponseDTO));
        // Act
        Flux<VisitResponseDTO> result = visitService.getVisitsForPet(petId);

        // Assert
        StepVerifier.create(result)
                .expectNext(visitResponseDTO)
                .verifyComplete();
    }

    @Test
    void getVisitsForStatus () {
        when(visitRepo.findAllByStatus(anyString())).thenReturn(Flux.just(visit1));
        when(entityDtoUtil.toVisitResponseDTO(any())).thenReturn(Mono.just(visitResponseDTO));


        StepVerifier.create(visitService.getVisitsForStatus(visitResponseDTO.getStatus().toString()))
                .expectNextMatches(visitDTO -> visitDTO.getVisitId().equals(visit1.getVisitId()))
                .expectComplete()
                .verify();
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


/*    @Test
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
    }*/

    @Test
    void addVisit () {
        // Arrange
        when(visitRepo.insert(any(Visit.class))).thenReturn(Mono.just(visit1));
        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(petResponseDTO));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));
        // This line ensures that a Flux<Visit> is returned, even if it's empty, to prevent NullPointerException
        when(visitRepo.findByVisitDateAndPractitionerId(any(LocalDateTime.class), anyString())).thenReturn(Flux.empty());
        when(entityDtoUtil.toVisitEntity(any())).thenReturn(visit1);


        when(entityDtoUtil.generateVisitIdString()).thenReturn("yourVisitId");
        when(visitRepo.insert(visit1)).thenReturn(Mono.just(visit1));
        when(entityDtoUtil.toVisitResponseDTO(any())).thenReturn(Mono.just(visitResponseDTO));
        // Act and Assert
        StepVerifier.create(visitService.addVisit(Mono.just(visitRequestDTO)))
                .consumeNextWith(visitDTO1 -> {
                    assertEquals(visit1.getDescription(), visitDTO1.getDescription());
                    assertEquals(visit1.getPetId(), visitDTO1.getPetId());
                    assertEquals(visit1.getVisitDate(), visitDTO1.getVisitDate());
                    assertEquals(visitResponseDTO.getPractitionerId(), visitDTO1.getPractitionerId());
                }).verifyComplete();

        // Verify that the methods were called with the expected arguments
        verify(visitRepo, times(1)).insert(any(Visit.class));
        verify(petsClient, times(1)).getPetById(anyString());
        verify(vetsClient, times(1)).getVetByVetId(anyString());
        verify(visitRepo, times(1)).findByVisitDateAndPractitionerId(any(LocalDateTime.class), anyString());
    }

    @Test
    void addVisit_NoConflictingVisits_InsertsNewVisit () {
        // Arrange
        LocalDateTime visitDate = LocalDateTime.now().plusDays(1);
        String description = "Test Description";
        String petId = "TestId";
        String practitionerId = "TestPractitionerId";
        Status status = Status.UPCOMING;

        VisitRequestDTO visitRequestDTO = new VisitRequestDTO();
        visitRequestDTO.setVisitDate(visitDate);
        visitRequestDTO.setDescription(description);
        visitRequestDTO.setPetId(petId);
        visitRequestDTO.setPractitionerId(practitionerId);
        visitRequestDTO.setStatus(status);

        Visit visit = new Visit(); // Create a Visit entity with appropriate data
        VisitResponseDTO visitResponseDTO = new VisitResponseDTO(); // Create a VisitResponseDTO with appropriate data

        // Mock the behavior of the methods
        when(visitRepo.insert(any(Visit.class))).thenReturn(Mono.just(visit));
        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(new PetResponseDTO()));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(new VetDTO()));
        when(visitRepo.findByVisitDateAndPractitionerId(any(LocalDateTime.class), anyString())).thenReturn(Flux.empty());
        when(entityDtoUtil.toVisitEntity(any())).thenReturn(visit1);
        when(entityDtoUtil.generateVisitIdString()).thenReturn("yourVisitId");
        when(visitRepo.insert(visit1)).thenReturn(Mono.just(visit1));
        when(entityDtoUtil.toVisitResponseDTO(any())).thenReturn(Mono.just(visitResponseDTO));

        // Act
        Mono<VisitResponseDTO> result = visitService.addVisit(Mono.just(visitRequestDTO));

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.equals(visitResponseDTO))
                .verifyComplete();

        verify(visitRepo, times(1)).insert(any(Visit.class));
    }

    @Test
    void addVisit_ConflictingVisits_ThrowsDuplicateTimeException () {
        // Arrange
        LocalDateTime visitDate = LocalDateTime.now().plusDays(1);
        String description = "Test Description";
        String petId = "TestId";
        String practitionerId = "TestPractitionerId";
        Status status = Status.UPCOMING;

        VisitRequestDTO visitRequestDTO = new VisitRequestDTO();
        visitRequestDTO.setVisitDate(visitDate);
        visitRequestDTO.setDescription(description);
        visitRequestDTO.setPetId(petId);
        visitRequestDTO.setPractitionerId(practitionerId);
        visitRequestDTO.setStatus(status);

        // Create an instance of existingVisit with required properties
        Visit existingVisit = buildVisit("meow");
        existingVisit.setVisitDate(visitDate); // Set the visit date to match the new request
        existingVisit.setPractitionerId(practitionerId); // Set the practitioner ID to match the new request


        PetResponseDTO mockPetResponse = new PetResponseDTO(); // Adjust as necessary
        VetDTO mockVetResponse = new VetDTO(); // Create a mock VetDTO, set any necessary fields if required

        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(mockPetResponse));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(mockVetResponse)); // This ensures a non-null Mono is returned
        when(visitRepo.findByVisitDateAndPractitionerId(any(), any()))
                .thenReturn(Flux.just(existingVisit)); // Return existingVisit in case of conflict
        when(entityDtoUtil.toVisitEntity(any())).thenReturn(visit1);
        when(entityDtoUtil.generateVisitIdString()).thenReturn("yourVisitId");
        when(visitRepo.insert(visit1)).thenReturn(Mono.just(visit1));
        when(entityDtoUtil.toVisitResponseDTO(any())).thenReturn(Mono.just(visitResponseDTO)); // This simulates finding a conflicting visit

        // Act
        Mono<VisitResponseDTO> result = visitService.addVisit(Mono.just(visitRequestDTO));

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof DuplicateTimeException
                        && throwable.getMessage().contains("A visit with the same time and practitioner already exists."))
                .verify();

        // Ensure no attempt was made to insert a new visit due to the conflict
        verify(visitRepo, times(0)).insert(any(Visit.class));
    }

    @Test
    public void testAddVisit_NoDescription () {
        // Arrange
        VisitRequestDTO requestDTO = buildVisitRequestDTO();
        Visit visit = buildVisit(requestDTO.getDescription());
        VisitResponseDTO visitResponseDTO = buildVisitResponseDTO();

        requestDTO.setDescription(null);
        // Mock the behavior of dependencies

        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(petResponseDTO));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));
        when(entityDtoUtil.toVisitEntity(requestDTO)).thenReturn(visit);
        when(entityDtoUtil.generateVisitIdString()).thenReturn("yourVisitId");
        when(visitRepo.insert(visit)).thenReturn(Mono.just(visit));
        when(entityDtoUtil.toVisitResponseDTO(visit)).thenReturn(Mono.just(visitResponseDTO));

        // Act
        Mono<VisitResponseDTO> result = visitService.addVisit(Mono.just(requestDTO));

        // Assert
        StepVerifier.create(result)
                .expectError(BadRequestException.class)
                .verify();
    }
    @Test
    public void testAddVisit_BadVisitDate () {
        // Arrange
        VisitRequestDTO requestDTO = buildVisitRequestDTO();
        Visit visit = buildVisit(requestDTO.getDescription());
        VisitResponseDTO visitResponseDTO = buildVisitResponseDTO();

        requestDTO.setVisitDate(null);
        // Mock the behavior of dependencies

        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(petResponseDTO));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));
        when(entityDtoUtil.toVisitEntity(requestDTO)).thenReturn(visit);
        when(entityDtoUtil.generateVisitIdString()).thenReturn("yourVisitId");
        when(visitRepo.insert(visit)).thenReturn(Mono.just(visit));
        when(entityDtoUtil.toVisitResponseDTO(visit)).thenReturn(Mono.just(visitResponseDTO));

        // Act
        Mono<VisitResponseDTO> result = visitService.addVisit(Mono.just(requestDTO));

        // Assert
        StepVerifier.create(result)
                .expectError(BadRequestException.class)
                .verify();
    }
    @Test
    public void testAddVisit_DateInThePast () {
        // Arrange
        VisitRequestDTO requestDTO = buildVisitRequestDTO();
        Visit visit = buildVisit(requestDTO.getDescription());
        VisitResponseDTO visitResponseDTO = buildVisitResponseDTO();

        requestDTO.setVisitDate(LocalDateTime.parse("2023-10-12T14:30"));

        // Mock the behavior of dependencies

        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(petResponseDTO));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));
        when(entityDtoUtil.toVisitEntity(requestDTO)).thenReturn(visit);
        when(entityDtoUtil.generateVisitIdString()).thenReturn("yourVisitId");
        when(visitRepo.insert(visit)).thenReturn(Mono.just(visit));
        when(entityDtoUtil.toVisitResponseDTO(visit)).thenReturn(Mono.just(visitResponseDTO));

        // Act
        Mono<VisitResponseDTO> result = visitService.addVisit(Mono.just(requestDTO));

        // Assert
        StepVerifier.create(result)
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    public void testAddVisit_PetIdNull () {
        // Arrange
        VisitRequestDTO requestDTO = buildVisitRequestDTO();
        Visit visit = buildVisit(requestDTO.getDescription());
        VisitResponseDTO visitResponseDTO = buildVisitResponseDTO();

        requestDTO.setPetId("");

        // Mock the behavior of dependencies

        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(petResponseDTO));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));
        when(entityDtoUtil.toVisitEntity(requestDTO)).thenReturn(visit);
        when(entityDtoUtil.generateVisitIdString()).thenReturn("yourVisitId");
        when(visitRepo.insert(visit)).thenReturn(Mono.just(visit));
        when(entityDtoUtil.toVisitResponseDTO(visit)).thenReturn(Mono.just(visitResponseDTO));

        // Act
        Mono<VisitResponseDTO> result = visitService.addVisit(Mono.just(requestDTO));

        // Assert
        StepVerifier.create(result)
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    public void testAddVisit_VetIdNull () {
        // Arrange
        VisitRequestDTO requestDTO = buildVisitRequestDTO();
        Visit visit = buildVisit(requestDTO.getDescription());
        VisitResponseDTO visitResponseDTO = buildVisitResponseDTO();

        requestDTO.setPractitionerId("");

        // Mock the behavior of dependencies

        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(petResponseDTO));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));
        when(entityDtoUtil.toVisitEntity(requestDTO)).thenReturn(visit);
        when(entityDtoUtil.generateVisitIdString()).thenReturn("yourVisitId");
        when(visitRepo.insert(visit)).thenReturn(Mono.just(visit));
        when(entityDtoUtil.toVisitResponseDTO(visit)).thenReturn(Mono.just(visitResponseDTO));

        // Act
        Mono<VisitResponseDTO> result = visitService.addVisit(Mono.just(requestDTO));

        // Assert
        StepVerifier.create(result)
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    public void testAddVisit_BadStatus() {
        // Arrange
        VisitRequestDTO requestDTO = buildVisitRequestDTO();
        Visit visit = buildVisit(requestDTO.getDescription());
        VisitResponseDTO visitResponseDTO = buildVisitResponseDTO();

        requestDTO.setStatus(Status.CANCELLED);

        // Mock the behavior of dependencies

        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(petResponseDTO));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));
        when(entityDtoUtil.toVisitEntity(requestDTO)).thenReturn(visit);
        when(entityDtoUtil.generateVisitIdString()).thenReturn("yourVisitId");
        when(visitRepo.insert(visit)).thenReturn(Mono.just(visit));
        when(entityDtoUtil.toVisitResponseDTO(visit)).thenReturn(Mono.just(visitResponseDTO));

        // Act
        Mono<VisitResponseDTO> result = visitService.addVisit(Mono.just(requestDTO));

        // Assert
        StepVerifier.create(result)
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    void updateStatusForVisitByVisitId_CONFIRMED() {
        String status = "CONFIRMED";

        when(visitRepo.save(any(Visit.class))).thenReturn(Mono.just(visit1));
        when(visitRepo.findByVisitId(anyString())).thenReturn(Mono.just(visit1));
        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(petResponseDTO));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));
        when(entityDtoUtil.toVisitResponseDTO(any())).thenReturn(Mono.just(visitResponseDTO));

        Mono<VisitResponseDTO> result = visitService.updateStatusForVisitByVisitId(visitResponseDTO.getVisitId(), status);

        StepVerifier.create(result)
                .expectNext(visitResponseDTO)
                .verifyComplete();
    }

    @Test
    void updateStatusForVisitByVisitId_COMPLETED() {
        String status = "COMPLETED";

        when(visitRepo.save(any(Visit.class))).thenReturn(Mono.just(visit1));
        when(visitRepo.findByVisitId(anyString())).thenReturn(Mono.just(visit1));
        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(petResponseDTO));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));
        when(entityDtoUtil.toVisitResponseDTO(any())).thenReturn(Mono.just(visitResponseDTO));

        Mono<VisitResponseDTO> result = visitService.updateStatusForVisitByVisitId(visitResponseDTO.getVisitId(), status);

        StepVerifier.create(result)
                .expectNext(visitResponseDTO)
                .verifyComplete();
    }
    @Test
    void updateStatusForVisitByVisitId_CANCELLED() {
        String status = "CANCELLED";

        when(visitRepo.save(any(Visit.class))).thenReturn(Mono.just(visit1));
        when(visitRepo.findByVisitId(anyString())).thenReturn(Mono.just(visit1));
        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(petResponseDTO));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));
        when(entityDtoUtil.toVisitResponseDTO(any())).thenReturn(Mono.just(visitResponseDTO));

        Mono<VisitResponseDTO> result = visitService.updateStatusForVisitByVisitId(visitResponseDTO.getVisitId(), status);

        StepVerifier.create(result)
                .expectNext(visitResponseDTO)
                .verifyComplete();
    }
    @Test
    void updateStatusForVisitByVisitId_UPCOMING() {
        String status = "UPCOMING";

        when(visitRepo.save(any(Visit.class))).thenReturn(Mono.just(visit1));
        when(visitRepo.findByVisitId(anyString())).thenReturn(Mono.just(visit1));
        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(petResponseDTO));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));
        when(entityDtoUtil.toVisitResponseDTO(any())).thenReturn(Mono.just(visitResponseDTO));

        Mono<VisitResponseDTO> result = visitService.updateStatusForVisitByVisitId(visitResponseDTO.getVisitId(), status);

        StepVerifier.create(result)
                .expectNext(visitResponseDTO)
                .verifyComplete();
    }
    @Test
    void updateVisit() {

        Mono<VisitRequestDTO> visitRequestDTOMono = buildRequestDtoMono();

        when(visitRepo.save(any(Visit.class))).thenReturn(Mono.just(visit1));
        when(visitRepo.findByVisitId(anyString())).thenReturn(Mono.just(visit1));
        when(petsClient.getPetById(anyString())).thenReturn(Mono.just(petResponseDTO));
        when(vetsClient.getVetByVetId(anyString())).thenReturn(Mono.just(vet));
        when(entityDtoUtil.toVisitResponseDTO(visit1)).thenReturn(Mono.just(visitResponseDTO));
        when(entityDtoUtil.toVisitEntity(any())).thenReturn(visit1);
        Mono<VisitResponseDTO> result = visitService.updateVisit(visitResponseDTO.getVisitId(), visitRequestDTOMono);
        // Execute the method under test
        StepVerifier.create(result)
                .expectNext(visitResponseDTO)
                .verifyComplete();
    }

    @Test
    void deleteVisitById_visitId_shouldSucceed () {
        //arrange
        String visitId = "73b5c112-5703-4fb7-b7bc-ac8186811ae1";

        Mockito.when(visitRepo.findByVisitId(visitId)).thenReturn(Mono.just(visit1));
        Mockito.when(visitRepo.deleteByVisitId(visitId)).thenReturn(Mono.empty());

        //act
        Mono<Void> expectResult = visitService.deleteVisit(visitId);

        //assert
        StepVerifier.create(expectResult)
                .verifyComplete();

        Mockito.verify(visitRepo, Mockito.times(1)).deleteByVisitId(visitId);

    }

    @Test
    void deleteVisitById_visitDoesNotExist_shouldThrowNotFoundException () {
        // Arrange
        String visitId = UUID.randomUUID().toString();

        // Mock the existsByVisitId method to return false, indicating that the visit does not exist
//        Mockito.when(visitRepo.existsByVisitId(visitId)).thenReturn(Mono.just(false));
        Mockito.when(visitRepo.findByVisitId(visitId)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = visitService.deleteVisit(visitId);

        // Assert
        StepVerifier.create(result)
                .expectError(NotFoundException.class) // Expecting NotFoundException
                .verify();

        // Verify that deleteByVisitId was not called since is does not exist
        Mockito.verify(visitRepo, Mockito.never()).deleteByVisitId(visitId);
    }

    @Test
    void deleteAllCancelledVisits () {

        // Arrange

        List<Visit> cancelledVisits = new ArrayList<>();
        cancelledVisits.add(buildVisit("Cat is sick"));
        cancelledVisits.add(buildVisit("Cat is sick"));
        cancelledVisits.forEach(visit -> visit.setStatus(Status.CANCELLED)); //set statuses to CANCELLED

        Mockito.when(visitRepo.findAllByStatus("CANCELLED")).thenReturn(Flux.fromIterable(cancelledVisits));
        Mockito.when(visitRepo.deleteAll(cancelledVisits)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = visitService.deleteAllCancelledVisits();

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        Mockito.verify(visitRepo, Mockito.times(1)).findAllByStatus("CANCELLED");
        Mockito.verify(visitRepo, Mockito.times(1)).deleteAll(cancelledVisits);
    }

    @Test
    void deleteAllCanceledVisits_shouldThrowRuntimeException () {
        // Arrange
        List<Visit> cancelledVisits = new ArrayList<>();
        cancelledVisits.add(buildVisit("Cat is sick"));
        cancelledVisits.add(buildVisit("Cat is sick"));
        cancelledVisits.forEach(visit -> visit.setStatus(Status.CANCELLED)); //set statuses to CANCELLED
        Mockito.when(visitRepo.findAllByStatus("CANCELLED")).thenReturn(Flux.fromIterable(cancelledVisits));
        Mockito.when(visitRepo.deleteAll(cancelledVisits)).thenReturn(Mono.error(new RuntimeException("Failed to delete visits")));

        // Act
        Mono<Void> result = visitService.deleteAllCancelledVisits();

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        Mockito.verify(visitRepo, Mockito.times(1)).findAllByStatus("CANCELLED");
        Mockito.verify(visitRepo, Mockito.times(1)).deleteAll(cancelledVisits);
    }


    private Visit buildVisit (String description){
        return Visit.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description(description)
                .petId("ecb109cd-57ea-4b85-b51e-99751fd1c349")
                .practitionerId("ecb109cd-57ea-4b85-b51e-99751fd1c342")
                .status(Status.UPCOMING)
                .build();
    }
    private VisitResponseDTO buildVisitResponseDTO () {
        return VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("ecb109cd-57ea-4b85-b51e-99751fd1c349")
                .practitionerId("ecb109cd-57ea-4b85-b51e-99751fd1c342")
                .status(Status.UPCOMING)
                .build();
    }
    private VisitRequestDTO buildVisitRequestDTO () {
        return VisitRequestDTO.builder()
                .visitDate(LocalDateTime.parse("2024-11-25 13:45", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .description("this is a dummy description")
                .petId("ecb109cd-57ea-4b85-b51e-99751fd1c349")
                .practitionerId("ecb109cd-57ea-4b85-b51e-99751fd1c342")
                .status(Status.UPCOMING)
                .build();
    }

    private Mono<VisitRequestDTO> buildRequestDtoMono () {
        VisitRequestDTO requestDTO = buildVisitRequestDTO();
        return Mono.just(requestDTO);
    }
}