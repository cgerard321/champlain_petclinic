package com.petclinic.vet.servicelayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.vet.dataaccesslayer.Education;
import com.petclinic.vet.dataaccesslayer.EducationRepository;
import com.petclinic.vet.dataaccesslayer.PhotoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.r2dbc.init.R2dbcScriptDatabaseInitializer;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
class EducationServiceImplTest {

    @Autowired
    EducationService educationService;

    @MockBean
    EducationRepository educationRepository;

    @MockBean
    ObjectMapper objectMapper;

    //To counter missing bean error
    @Autowired
    PhotoRepository photoRepository;
    @MockBean
    ConnectionFactoryInitializer connectionFactoryInitializer;
    @MockBean
    R2dbcScriptDatabaseInitializer r2dbcScriptDatabaseInitializer;

    String Vet_Id = "1";
    Education education = buildEducation();

    @Test
    void getAllEducationsByVetId() {
        when(educationRepository.findAllByVetId(anyString())).thenReturn(Flux.just(education));

        Flux<EducationResponseDTO> educationResponseDTO = educationService.getAllEducationsByVetId("1");

        StepVerifier
                .create(educationResponseDTO)
                .consumeNextWith(found -> {
                    assertEquals(education.getEducationId(), found.getEducationId());
                    assertEquals(education.getVetId(), found.getVetId());
                })
                .verifyComplete();
    }
    @Test
    void deleteEducationByEducationId() {
        when(educationRepository.findByVetIdAndEducationId(anyString(), anyString())).thenReturn(Mono.just(education));
        when(educationRepository.delete(any())).thenReturn(Mono.empty());

        Mono<Void> deletedEducation = educationService.deleteEducationByEducationId(education.getVetId(), education.getEducationId());

        StepVerifier
                .create(deletedEducation)
                .verifyComplete();
    }

    private Education buildEducation() {
        return Education.builder()
                .educationId("1")
                .vetId("1")
                .schoolName("test school")
                .degree("test degree")
                .fieldOfStudy("test field")
                .startDate("test start year")
                .endDate("test end year")
                .build();
    }


}