package com.petclinic.visits.visitsservicenew.PresentationLayer;


import com.petclinic.visits.visitsservicenew.BusinessLayer.VisitService;
import com.petclinic.visits.visitsservicenew.DataLayer.VisitDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = VisitController.class)
public class VisitControllerUnitTest {

    private VisitDTO dto = buildVisitDto();
    private final String Visit_UUID_OK = dto.getVisitId();
    private final int Practitioner_Id_OK = dto.getPractitionerId();

    private final int Pet_Id_OK = dto.getPetId();

    private final int Get_Month = dto.getMonth();

    @Autowired
    private WebTestClient webFluxTest;
    
    @MockBean
    VisitService visitService;

    @Test
    public void getVisitByVisitId(){
        
        when(visitService.getVisitByVisitId(anyString())).thenReturn(Mono.just(dto));
        
        webFluxTest.get()
                .uri("http://localhost:8080/visits/" + Visit_UUID_OK)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.visitId").isEqualTo(dto.getVisitId())
                .jsonPath("$.year").isEqualTo(dto.getYear())
                .jsonPath("$.month").isEqualTo(dto.getMonth())
                .jsonPath("$.day").isEqualTo(dto.getDay())
                .jsonPath("$.description").isEqualTo(dto.getDescription())
                .jsonPath("$.petId").isEqualTo(dto.getPetId())
                .jsonPath("$.practitionerId").isEqualTo(dto.getPractitionerId())
                .jsonPath("$.status").isEqualTo(dto.isStatus());

        Mockito.verify(visitService, times(1))
                .getVisitByVisitId(Visit_UUID_OK);

    }

    @Test
    public void getVisitByPractitionerId(){

        when(visitService.getVisitsForPractitioner(anyInt())).thenReturn(Flux.just(dto));

        webFluxTest.get()
                .uri("http://localhost:8080/visits/practitioner/visits/" + Practitioner_Id_OK)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);

        Mockito.verify(visitService, times(1))
                .getVisitsForPractitioner(Practitioner_Id_OK);

    }

    @Test
    public void getVisitsByPetId(){

        when(visitService.getVisitsForPet(anyInt())).thenReturn(Flux.just(dto));

        webFluxTest.get()
                .uri("http://localhost:8080/visits/pets/" + Pet_Id_OK)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);

        Mockito.verify(visitService, times(1))
                .getVisitsForPet(Pet_Id_OK);

    }

    @Test
    public void getByPractitionerIdAndMonth(){

        when(visitService.getVisitsByPractitionerIdAndMonth(anyInt(), anyInt())).thenReturn(Flux.just(dto));

        webFluxTest.get()
                .uri("http://localhost:8080/visits/practitioner/" + Practitioner_Id_OK+ "/" + Get_Month)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);

        Mockito.verify(visitService, times(1))
                .getVisitsByPractitionerIdAndMonth(Practitioner_Id_OK, Get_Month);

    }
    @Test
    public void addVisit(){

        VisitDTO visitDtoInsert = new VisitDTO("1", 2022, 11, 24, "This is awesome", 2, 2, true);


        when(visitService.addVisit(Mono.just(visitDtoInsert))).thenReturn(Mono.just(visitDtoInsert));

        webFluxTest.post()
                .uri("http://localhost:8080/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(visitDtoInsert))
                .exchange()
                .expectStatus().isOk();

        Mockito.verify(visitService, times(1))
                .addVisit(Mono.just(visitDtoInsert));

    }

    @Test
    public void updateVisitByVisitId(){

        when(visitService.updateVisit(anyString(), Mono.just(dto))).thenReturn(Mono.just(dto));

        webFluxTest.put()
                .uri("http://localhost:8080/visits/visits/", Visit_UUID_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(dto))
                .exchange()
                .expectStatus().isAccepted();

        Mockito.verify(visitService, times(1)).updateVisit(Visit_UUID_OK, Mono.just(dto));
    }
    @Test
    public void deleteVisit(){

        webFluxTest.delete()
                .uri("http://localhost:8080/visits/" + Visit_UUID_OK)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        Mockito.verify(visitService, times(1)).deleteVisit(Visit_UUID_OK);
    }

    private VisitDTO buildVisitDto(){

        return VisitDTO.builder()
                .visitId("73b5c112-5703-4fb7-b7bc-ac8186811ae1")
                .year(2022)
                .month(11)
                .day(24)
                .description("this is a dummy description")
                .petId(2)
                .practitionerId(2)
                .status(true).build();
    }

}
