package com.petclinic.visits.visitsservicenew.BusinessLayer.Emergency;

import com.petclinic.visits.visitsservicenew.DataLayer.Emergency.Emergency;
import com.petclinic.visits.visitsservicenew.DataLayer.Emergency.EmergencyRepository;
import com.petclinic.visits.visitsservicenew.DataLayer.Emergency.UrgencyLevel;
import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.PetResponseDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.PetsClient;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetDTO;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.VetsClient;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Emergency.EmergencyRequestDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.Emergency.EmergencyResponseDTO;
import com.petclinic.visits.visitsservicenew.PresentationLayer.VisitResponseDTO;
import com.petclinic.visits.visitsservicenew.Utils.EntityDtoUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
@AutoConfigureWebTestClient
class EmergencyServiceImplTest {

    @Autowired
    private EmergencyServiceImpl emergencyService;

    @MockBean
    private EmergencyRepository emergencyRepository;

    @MockBean
    private VetsClient vetsClient;

    @MockBean
    private PetsClient petsClient;

    @MockBean
    private EntityDtoUtil entityDtoUtil;

    String uuidVet = UUID.randomUUID().toString();
    String uuidPet = UUID.randomUUID().toString();
    String uuidPhoto = UUID.randomUUID().toString();
    String uuidOwner = UUID.randomUUID().toString();

    Date currentDate = new Date();
    PetResponseDTO petResponseDTO = PetResponseDTO.builder()
            .petTypeId(uuidPet)
            .name("Billy")
            .birthDate(currentDate)
            .photoId(uuidPhoto)
            .ownerId(uuidOwner)
            .build();

    @Test
    void getAllEmergencies() {
        // Arrange
        Emergency emergency1 = new Emergency(); // Initialize with test data
        emergency1.setVisitEmergencyId("1");
        emergency1.setVisitDate(LocalDateTime.now());
        emergency1.setDescription("Dog bite");
        emergency1.setPetId("pet-1");
        emergency1.setPractitionerId("vet-1");
        emergency1.setUrgencyLevel(UrgencyLevel.HIGH);
        emergency1.setEmergencyType("Injury");

        Emergency emergency2 = new Emergency(); // Another emergency
        emergency2.setVisitEmergencyId("2");
        emergency2.setVisitDate(LocalDateTime.now());
        emergency2.setDescription("Poisoning");
        emergency2.setPetId("pet-2");
        emergency2.setPractitionerId("vet-2");
        emergency2.setUrgencyLevel(UrgencyLevel.LOW);
        emergency2.setEmergencyType("Injury");

        // Mock the behavior of the emergency repository
        when(emergencyRepository.findAll()).thenReturn(Flux.just(emergency1, emergency2));

        // Mock the behavior of petsClient and vetsClient
        when(petsClient.getPetById(emergency1.getPetId())).thenReturn(Mono.just(
                PetResponseDTO.builder().name("Buddy").birthDate(new Date()).build()));
        when(petsClient.getPetById(emergency2.getPetId())).thenReturn(Mono.just(
                PetResponseDTO.builder().name("Max").birthDate(new Date()).build()));

        when(vetsClient.getVetByVetId(emergency1.getPractitionerId())).thenReturn(Mono.just(
                VetDTO.builder().firstName("John").lastName("Doe").email("john.doe@email.com").phoneNumber("(514)-123-4567").build()));
        when(vetsClient.getVetByVetId(emergency2.getPractitionerId())).thenReturn(Mono.just(
                VetDTO.builder().firstName("Jane").lastName("Smith").email("jane.smith@email.com").phoneNumber("(514)-765-4321").build()));

        // Mock the utility method to return the mapped EmergencyResponseDTO
        EmergencyResponseDTO responseDTO1 = EmergencyResponseDTO.builder()
                .visitEmergencyId(emergency1.getVisitEmergencyId())
                .visitDate(emergency1.getVisitDate())
                .description(emergency1.getDescription())
                .petId(emergency1.getPetId())
                .petName("Buddy") // Pet name from mocked pet
                .petBirthDate(new Date())
                .practitionerId(emergency1.getPractitionerId())
                .vetFirstName("John")
                .vetLastName("Doe")
                .vetEmail("john.doe@email.com")
                .vetPhoneNumber("(514)-123-4567")
                .urgencyLevel(emergency1.getUrgencyLevel())
                .emergencyType(emergency1.getEmergencyType())
                .build();

        EmergencyResponseDTO responseDTO2 = EmergencyResponseDTO.builder()
                .visitEmergencyId(emergency2.getVisitEmergencyId())
                .visitDate(emergency2.getVisitDate())
                .description(emergency2.getDescription())
                .petId(emergency2.getPetId())
                .petName("Max") // Pet name from mocked pet
                .petBirthDate(new Date()) // Replace with the actual birth date if available
                .practitionerId(emergency2.getPractitionerId())
                .vetFirstName("Jane")
                .vetLastName("Smith")
                .vetEmail("jane.smith@email.com")
                .vetPhoneNumber("(514)-765-4321")
                .urgencyLevel(emergency2.getUrgencyLevel())
                .emergencyType(emergency2.getEmergencyType())
                .build();

        when(entityDtoUtil.toEmergencyResponseDTO(emergency1)).thenReturn(Mono.just(responseDTO1));
        when(entityDtoUtil.toEmergencyResponseDTO(emergency2)).thenReturn(Mono.just(responseDTO2));

        // Act
        Flux<EmergencyResponseDTO> result = emergencyService.GetAllEmergencies();

        // Verify the results using StepVerifier
        StepVerifier.create(result)
                .expectNext(responseDTO1) // Expect the mapped EmergencyResponseDTO for the first emergency
                .expectNext(responseDTO2) // Expect the mapped EmergencyResponseDTO for the second emergency
                .verifyComplete();
    }

    @Test
    public void getVisitsForPet () {
        // Arrange
        String petId = "yourPetId";

        Emergency emergency1 = new Emergency(); // Initialize with test data
        emergency1.setVisitEmergencyId("1");
        emergency1.setVisitDate(LocalDateTime.now());
        emergency1.setDescription("Dog bite");
        emergency1.setPetId("pet-1");
        emergency1.setPractitionerId("vet-1");
        emergency1.setUrgencyLevel(UrgencyLevel.HIGH);
        emergency1.setEmergencyType("Injury");

        EmergencyResponseDTO responseDTO1 = EmergencyResponseDTO.builder()
                .visitEmergencyId(emergency1.getVisitEmergencyId())
                .visitDate(emergency1.getVisitDate())
                .description(emergency1.getDescription())
                .petId(emergency1.getPetId())
                .petName("Buddy") // Pet name from mocked pet
                .petBirthDate(new Date())
                .practitionerId(emergency1.getPractitionerId())
                .vetFirstName("John")
                .vetLastName("Doe")
                .vetEmail("john.doe@email.com")
                .vetPhoneNumber("(514)-123-4567")
                .urgencyLevel(emergency1.getUrgencyLevel())
                .emergencyType(emergency1.getEmergencyType())
                .build();


        // Mock the behavior of dependencies
        when(emergencyRepository.findByPetId(petId)).thenReturn(Flux.just(emergency1));
        when(entityDtoUtil.toEmergencyResponseDTO(emergency1)).thenReturn(Mono.just(responseDTO1));
        when(petsClient.getPetById(petId)).thenReturn(Mono.just(petResponseDTO));
        // Act
        Flux<EmergencyResponseDTO> result = emergencyService.getEmergencyVisitsForPet(petId);

        // Assert
        StepVerifier.create(result)
                .expectNext(responseDTO1)
                .verifyComplete();
    }

    @Test
    public void getEmergencyVisitsForPet_notFound() {
        // Arrange
        String petId = "nonExistentPetId";

        // Mock petsClient to return Mono.empty(), simulating a non-existent pet
        when(petsClient.getPetById(petId)).thenReturn(Mono.empty()); // Pet not found

        // No emergencies for the pet because it doesn't exist
        when(emergencyRepository.findByPetId(petId)).thenReturn(Flux.empty());

        // Act
        Flux<EmergencyResponseDTO> result = emergencyService.getEmergencyVisitsForPet(petId);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException &&
                        throwable.getMessage().equals("No pet was found with petId: " + petId))
                .verify();
    }


    @Test
    void addEmergency() {
        // Arrange
        String uuidEmergency = UUID.randomUUID().toString();
        String uuidVet = UUID.randomUUID().toString();
        String uuidPet = UUID.randomUUID().toString();

        // Mock Emergency entity to save
        Emergency emergency = Emergency.builder()
                .visitEmergencyId(uuidEmergency)
                .visitDate(LocalDateTime.now())
                .description("Severe injury")
                .petId(uuidPet)
                .practitionerId(uuidVet)
                .urgencyLevel(UrgencyLevel.HIGH)
                .emergencyType("Injury")
                .build();

        // Request DTO
        EmergencyRequestDTO requestDTO = EmergencyRequestDTO.builder()
                .visitDate(LocalDateTime.now())
                .description("Severe injury")
                .petId(uuidPet)
                .practitionerId(uuidVet)
                .urgencyLevel(UrgencyLevel.HIGH)
                .emergencyType("Injury")
                .build();

        // Mock responses for dependencies
        when(petsClient.getPetById(uuidPet)).thenReturn(Mono.just(
                PetResponseDTO.builder().name("Buddy").birthDate(new Date()).build()));

        when(vetsClient.getVetByVetId(uuidVet)).thenReturn(Mono.just(
                VetDTO.builder().firstName("John").lastName("Doe").build()));

        // Mock repository save behavior
        when(emergencyRepository.save(any(Emergency.class))).thenReturn(Mono.just(emergency));

        // Mock transformation to EmergencyResponseDTO
        EmergencyResponseDTO responseDTO = EmergencyResponseDTO.builder()
                .visitEmergencyId(uuidEmergency)
                .visitDate(emergency.getVisitDate())
                .description(emergency.getDescription())
                .petId(uuidPet)
                .petName("Buddy") // Pet name from mocked pet
                .petBirthDate(new Date())
                .practitionerId(uuidVet)
                .vetFirstName("John")
                .vetLastName("Doe")
                .vetEmail("john.doe@email.com")
                .vetPhoneNumber("(514)-123-4567")
                .urgencyLevel(emergency.getUrgencyLevel())
                .emergencyType(emergency.getEmergencyType())
                .build();

        when(entityDtoUtil.toEmergencyResponseDTO(emergency)).thenReturn(Mono.just(responseDTO));


        // Act
        Mono<EmergencyResponseDTO> result = emergencyService.AddEmergency(Mono.just(requestDTO));

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getVisitEmergencyId().equals(uuidEmergency)
                        && dto.getPetName().equals("Buddy")
                        && dto.getVetFirstName().equals("John")
                        && dto.getUrgencyLevel() == UrgencyLevel.HIGH
                        && dto.getEmergencyType().equals("Injury"))
                .verifyComplete();
    }


    @Test
    public void whenGetEmergencyByVisitEmergencyId_thenReturnEmergency() {
        // Arrange: Create mock Emergency and EmergencyResponseDTO objects
        Emergency emergency1 = Emergency.builder()
                .visitEmergencyId("uuidEmergency")
                .visitDate(LocalDateTime.now())
                .description("Severe injury")
                .petId(uuidPet)
                .practitionerId(uuidVet)
                .urgencyLevel(UrgencyLevel.HIGH)
                .emergencyType("Injury")
                .build();

        EmergencyResponseDTO responseDTO = EmergencyResponseDTO.builder()
                .visitEmergencyId("uuidEmergency")
                .visitDate(emergency1.getVisitDate())
                .description(emergency1.getDescription())
                .petId(uuidPet)
                .petName("Buddy") // Mocked pet name
                .petBirthDate(new Date()) // Mocked date
                .practitionerId(uuidVet)
                .vetFirstName("John")
                .vetLastName("Doe")
                .vetEmail("john.doe@email.com")
                .vetPhoneNumber("(514)-123-4567")
                .urgencyLevel(emergency1.getUrgencyLevel())
                .emergencyType(emergency1.getEmergencyType())
                .build();

        // Mock the repository and the entity to DTO conversion
        when(emergencyRepository.findEmergenciesByVisitEmergencyId(anyString())).thenReturn(Mono.just(emergency1));
        when(entityDtoUtil.toEmergencyResponseDTO(any(Emergency.class))).thenReturn(Mono.just(responseDTO));

        // Act & Assert: Verify the result using StepVerifier
        StepVerifier.create(emergencyService.GetEmergencyByEmergencyId(responseDTO.getVisitEmergencyId()))
                .expectNextMatches(visitDTO ->
                        visitDTO.getVisitEmergencyId().equals(emergency1.getVisitEmergencyId()) &&
                                visitDTO.getVetFirstName().equals("John") &&
                                visitDTO.getVetLastName().equals("Doe") &&
                                visitDTO.getUrgencyLevel() == emergency1.getUrgencyLevel()
                )
                .expectComplete()
                .verify();
    }



}