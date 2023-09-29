package com.petclinic.vet.servicelayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.vet.dataaccesslayer.Education;
import com.petclinic.vet.dataaccesslayer.EducationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
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