package com.petclinic.visits.visitsservicenew.PresentationLayer;


import com.petclinic.visits.visitsservicenew.BusinessLayer.VisitService;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@WebFluxTest(VisitController.class)
class VisitControllerUnitTest {
    @MockBean
    private VisitService visitService;
    @Autowired
    private WebTestClient webFluxTest;

    private final VisitResponseDTO visitResponseDTO = buildVisitResponseDto();
    private final VisitRequestDTO visitRequestDTO = buildVisitRequestDTO();
    private final String Visit_UUID_OK = visitResponseDTO.getVisitId();
    private final int Practitioner_Id_OK = visitResponseDTO.getPractitionerId();
    private final int Pet_Id_OK = visitResponseDTO.getPetId();
    private final int Get_Month = visitResponseDTO.getMonth();

    @Test
    void getAllVisits(){
        when(visitService.getAllVisits()).thenReturn(Flux.just(visitResponseDTO, visitResponseDTO));

        webFluxTest.get()
                .uri("/visits")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM + ";charset=UTF-8")
                .returnResult(VisitResponseDTO.class);

        Mockito.verify(visitService, times(1)).getAllVisits();
    }

    @Test
    void getVisitByVisitId(){
        when(visitService.getVisitByVisitId(anyString())).thenReturn(Mono.just(visitResponseDTO));

        webFluxTest.get()
                .uri("/visits/" + Visit_UUID_OK)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(visitResponseDTO.getVisitId())
                .jsonPath("$.year").isEqualTo(visitResponseDTO.getYear())
                .jsonPath("$.month").isEqualTo(visitResponseDTO.getMonth())
                .jsonPath("$.day").isEqualTo(visitResponseDTO.getDay())
                .jsonPath("$.description").isEqualTo(visitResponseDTO.getDescription())
                .jsonPath("$.petId").isEqualTo(visitResponseDTO.getPetId())
                .jsonPath("$.practitionerId").isEqualTo(visitResponseDTO.getPractitionerId())
                .jsonPath("$.status").isEqualTo(visitResponseDTO.isStatus());

        Mockito.verify(visitService, times(1)).getVisitByVisitId(Visit_UUID_OK);
    }

    @Test
    void getVisitByPractitionerId(){
        when(visitService.getVisitsForPractitioner(anyInt())).thenReturn(Flux.just(visitResponseDTO));

        webFluxTest.get()
                .uri("/visits/practitioner/visits/" + Practitioner_Id_OK)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .returnResult(VisitResponseDTO.class);

        Mockito.verify(visitService, times(1)).getVisitsForPractitioner(Practitioner_Id_OK);
    }

    @Test
    void getVisitsByPetId(){
        when(visitService.getVisitsForPet(anyInt())).thenReturn(Flux.just(visitResponseDTO));

        webFluxTest.get()
                .uri("/visits/pets/" + Pet_Id_OK)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM + ";charset=UTF-8")
                .returnResult(VisitResponseDTO.class);

        Mockito.verify(visitService, times(1)).getVisitsForPet(Pet_Id_OK);
    }

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

    @Test
    void addVisit(){
        when(visitService.addVisit(any(Mono.class))).thenReturn(Mono.just(visitResponseDTO));

        webFluxTest
                .post()
                .uri("/visits")
                .body(Mono.just(visitRequestDTO), VisitRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        Mockito.verify(visitService, times(1)).addVisit(any(Mono.class));
    }

    @Test
    void updateVisitByVisitId(){
        Mono<VisitRequestDTO> monoVisit = Mono.just(visitRequestDTO);
        when(visitService.updateVisit(anyString(), any(Mono.class))).thenReturn(monoVisit);//for some reason this code here returns null

        webFluxTest.put()
                .uri("/visits/visits/" + Visit_UUID_OK)
                .accept(MediaType.APPLICATION_JSON)
                .body(monoVisit, VisitRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();

        Mockito.verify(visitService, times(1)).updateVisit(anyString(), any(Mono.class));
    }
    @Test
    void deleteVisit(){
        webFluxTest.delete()
                .uri("/visits/" + Visit_UUID_OK)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        Mockito.verify(visitService, times(1)).deleteVisit(Visit_UUID_OK);
    }
    private VisitResponseDTO buildVisitResponseDto(){
        return VisitResponseDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .year(2022)
                .month(11)
                .day(24)
                .description("this is a dummy description")
                .petId(2)
                .practitionerId(2)
                .status(true).build();
    }
    private VisitRequestDTO buildVisitRequestDTO(){
        return VisitRequestDTO.builder()
                .year(2022)
                .month(11)
                .day(24)
                .description("this is a dummy description")
                .petId(2)
                .practitionerId(2)
                .status(true).build();
    }
}