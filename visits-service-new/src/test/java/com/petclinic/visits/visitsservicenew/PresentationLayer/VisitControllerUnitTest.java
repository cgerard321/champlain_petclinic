package com.petclinic.visits.visitsservicenew.PresentationLayer;


import com.petclinic.visits.visitsservicenew.BusinessLayer.VisitService;
import com.petclinic.visits.visitsservicenew.DataLayer.Visit;
import com.petclinic.visits.visitsservicenew.DomainClientLayer.*;
import com.petclinic.visits.visitsservicenew.Exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@WebFluxTest(VisitController.class)
class VisitControllerUnitTest {
    @MockBean
    private VisitService visitService;

    @Autowired
    private WebTestClient webTestClient;


    @MockBean
    private VetsClient vetsClient;

    @MockBean
    private PetsClient petsClient;

    String uuid1 = UUID.randomUUID().toString();
    String uuid2 = UUID.randomUUID().toString();
    String uuid3 = UUID.randomUUID().toString();
    String uuid4 = UUID.randomUUID().toString();


    Set<SpecialtyDTO> set= new HashSet<>();


    VetDTO vet = VetDTO.builder()
            .vetId(uuid1)
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
            .petTypeId(uuid2)
            .name("Billy")
            .birthDate(currentDate)
            .photoId(uuid3)
            .ownerId(uuid4)
            .build();

    Visit visit1 = buildVisit(uuid1,"this is a dummy description",vet.getVetId());
    private final VisitResponseDTO visitResponseDTO = buildVisitResponseDto();
    private final VisitRequestDTO visitRequestDTO = buildVisitRequestDTO(vet.getVetId());
    private final String Visit_UUID_OK = visitResponseDTO.getVisitId();
    private final String Practitioner_Id_OK = visitResponseDTO.getPractitionerId();
    private final String Pet_Id_OK = visitResponseDTO.getPetId();
    //private final LocalDateTime visitDate = visitResponseDTO.getVisitDate().withSecond(0);
    @Test
    void getAllVisits(){
        when(visitService.getAllVisits()).thenReturn(Flux.just(visitResponseDTO, visitResponseDTO));

        webTestClient.get()
                .uri("/visits")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM + ";charset=UTF-8")
                .returnResult(VisitResponseDTO.class);

        verify(visitService, times(1)).getAllVisits();
    }

    @Test
    void getVisitByVisitId(){
        when(visitService.getVisitByVisitId(anyString())).thenReturn(Mono.just(visitResponseDTO));

        webTestClient.get()
                .uri("/visits/" + Visit_UUID_OK)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(visitResponseDTO.getVisitId())
                .jsonPath("$.visitDate").isEqualTo("2022-11-25T13:45:00")
                .jsonPath("$.description").isEqualTo(visitResponseDTO.getDescription())
                .jsonPath("$.petId").isEqualTo(visitResponseDTO.getPetId())
                .jsonPath("$.practitionerId").isEqualTo(visitResponseDTO.getPractitionerId())
                .jsonPath("$.status").isEqualTo(visitResponseDTO.isStatus());

        verify(visitService, times(1)).getVisitByVisitId(Visit_UUID_OK);
    }

    @Test
    void getVisitByPractitionerId(){
        when(visitService.getVisitsForPractitioner(anyString())).thenReturn(Flux.just(visitResponseDTO));

<<<<<<< HEAD
        webFluxTest.get()
                .uri("/visits/practitioner/" + Practitioner_Id_OK)
=======
        webTestClient.get()
                .uri("/visits/veterinarians/" + Vet_Id_OK)
>>>>>>> 0d8aafd3 (Modified some backend logic to be more reactive and updated some test methods)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .returnResult(VisitResponseDTO.class);

<<<<<<< HEAD
        Mockito.verify(visitService, times(1)).getVisitsForPractitioner(Practitioner_Id_OK);
=======
        verify(visitService, times(1)).getVisitsForVet(Vet_Id_OK);
>>>>>>> 0d8aafd3 (Modified some backend logic to be more reactive and updated some test methods)
    }

    @Test
    void getVisitsByPetId(){
        when(visitService.getVisitsForPet(anyString())).thenReturn(Flux.just(visitResponseDTO));

        webTestClient.get()
                .uri("/visits/pets/" + Pet_Id_OK)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM + ";charset=UTF-8")
                .returnResult(VisitResponseDTO.class);

        verify(visitService, times(1)).getVisitsForPet(Pet_Id_OK);
    }

    /*
    @Test
    void getByPractitionerIdAndMonth(){
        when(visitService.getVisitsByPractitionerIdAndMonth(anyInt(), anyInt())).thenReturn(Flux.just(visitResponseDTO));

        webFluxTest.get()
                .uri("/visits/practitioner/" + Practitioner_Id_OK+ "/" + Get_Month)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM + ";charset=UTF-8")
                .returnResult(VisitResponseDTO.class);

        Mockito.verify(visitService, times(1)).getVisitsByPractitionerIdAndMonth(Practitioner_Id_OK, Get_Month);
    }
     */

    @Test
    void addVisit(){
        when(visitService.addVisit(any(Mono.class))).thenReturn(Mono.just(visitResponseDTO));

        webTestClient
                .post()
                .uri("/visits")
                .body(Mono.just(visitRequestDTO), VisitRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        verify(visitService, times(1)).addVisit(any(Mono.class));
    }

    @Test
    void updateVisitByVisitId(){


        Mono<VisitRequestDTO> monoVisit = Mono.just(visitRequestDTO);


        webTestClient.put()
                .uri("/visits/" + Visit_UUID_OK)
                .accept(MediaType.APPLICATION_JSON)
                .body(monoVisit, VisitRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        verify(visitService, times(1)).updateVisit(anyString(), any(Mono.class));
    }
    /*
    @Test
    void deleteVisit(){
        webFluxTest.delete()
                .uri("/visits/" + Visit_UUID_OK)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        Mockito.verify(visitService, times(1)).deleteVisit(Visit_UUID_OK);
    }
     */
    @Test
    void deleteVisit_visitId_shouldSucceed() {
        // Arrange
        String visitId = UUID.randomUUID().toString();
        when(visitService.deleteVisit(visitId)).thenReturn(Mono.empty());

        // Act & Assert
        webTestClient
                .delete()
                .uri("/visits/{visitId}", visitId)
                .exchange()
                .expectStatus().isNoContent();  // Expecting 204 NO CONTENT status.

        verify(visitService, times(1)).deleteVisit(visitId);
    }

    @Test
    void deleteVisit_NonExistentVisitId_ShouldReturnNotFound() {
        // Arrange
        String invalidVisitId = "fakeId";
        when(visitService.deleteVisit(invalidVisitId)).thenReturn(Mono.error(new NotFoundException("No visit was found with visitId: " + invalidVisitId)));

        // Act & Assert
        webTestClient
                .delete()
                .uri("/visits/{visitId}", invalidVisitId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message", "No visit was found with visitId: " + invalidVisitId);
    }



    private VisitResponseDTO buildVisitResponseDto(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .visitDate(LocalDateTime.parse("2022-11-25T13:45:00", dtf))
                .description("this is a dummy description")
                .petId("2")
                .practitionerId(UUID.randomUUID().toString())
                .status(true).build();
    }
    private VisitRequestDTO buildVisitRequestDTO(String vetId){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return VisitRequestDTO.builder()
                .visitDate(LocalDateTime.parse("2022-11-25T13:45:00", dtf))
                .description("this is a dummy description")
                .petId("2")
                .practitionerId(UUID.randomUUID().toString())
                .status(true).build();
    }

    private Visit buildVisit(String uuid, String description, String vetId){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        return Visit.builder()
                .visitId(uuid)
                .visitDate(LocalDateTime.parse("2022-11-25T13:45", dtf))
                .description(description)
                .petId("2")
                .practitionerId(vetId)
                .status(true).build();
    }
}