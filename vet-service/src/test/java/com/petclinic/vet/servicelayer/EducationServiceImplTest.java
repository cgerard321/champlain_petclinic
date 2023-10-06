package com.petclinic.vet.servicelayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.vet.dataaccesslayer.education.Education;
import com.petclinic.vet.dataaccesslayer.education.EducationRepository;
import com.petclinic.vet.dataaccesslayer.PhotoRepository;
import com.petclinic.vet.servicelayer.education.EducationRequestDTO;
import com.petclinic.vet.servicelayer.education.EducationResponseDTO;
import com.petclinic.vet.servicelayer.education.EducationService;
import com.petclinic.vet.dataaccesslayer.Vet;
import com.petclinic.vet.dataaccesslayer.VetRepository;
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

import java.util.HashSet;

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
    @MockBean
    VetRepository vetRepository;

    //To counter missing bean error
    @Autowired
    PhotoRepository photoRepository;
    @MockBean
    ConnectionFactoryInitializer connectionFactoryInitializer;
    @MockBean
    R2dbcScriptDatabaseInitializer r2dbcScriptDatabaseInitializer;

    String Vet_Id = "1";

    Vet existingVet=buildVet();

    String VET_ID = "vetId";
    Education education = buildEducation();
    EducationRequestDTO educationRequestDTO = buildEducationRequestDTO();


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
    @Test
    void updateEducationOfVet(){
        when(educationRepository.save(any())).thenReturn(Mono.just(education));
        when(vetRepository.findVetByVetId(anyString())).thenReturn(Mono.just(existingVet));
        when(educationRepository.findByVetIdAndEducationId(anyString(), anyString())).thenReturn(Mono.just(education));

        Mono<EducationResponseDTO> educationResponseDTO=educationService.updateEducationByVetIdAndEducationId(existingVet.getVetId(), education.getEducationId(), Mono.just(educationRequestDTO));

        StepVerifier
                .create(educationResponseDTO)
                .consumeNextWith(existingEducation -> {
                    assertNotNull(education.getId());
                    assertEquals(education.getEducationId(), existingEducation.getEducationId());
                    assertEquals(education.getVetId(), existingEducation.getVetId());
                    assertEquals(education.getSchoolName(), existingEducation.getSchoolName());
                    assertEquals(education.getDegree(), existingEducation.getDegree());
                    assertEquals(education.getFieldOfStudy(), existingEducation.getFieldOfStudy());
                    assertEquals(education.getStartDate(), existingEducation.getStartDate());
                    assertEquals(education.getEndDate(), existingEducation.getEndDate());
                })
                .verifyComplete();
    }

    @Test
    void addEducationToVet() {
        EducationRequestDTO educationRequestDTO = buildEducationRequestDTO();

        educationService.addEducationToVet(education.getVetId(), Mono.just(educationRequestDTO))
                .map(educationResponseDTO -> {
                    assertEquals(educationResponseDTO.getVetId(), educationRequestDTO.getVetId());
                    assertEquals(educationResponseDTO.getSchoolName(), educationRequestDTO.getSchoolName());
                    assertEquals(educationResponseDTO.getDegree(), educationRequestDTO.getDegree());
                    assertEquals(educationResponseDTO.getFieldOfStudy(), educationRequestDTO.getFieldOfStudy());
                    assertEquals(educationResponseDTO.getStartDate(), educationRequestDTO.getStartDate());
                    assertEquals(educationResponseDTO.getEndDate(), educationRequestDTO.getEndDate());
                    assertNotNull(educationResponseDTO.getEducationId());
                    return educationResponseDTO;
                });
    }

    private EducationRequestDTO buildEducationRequestDTO() {
        return EducationRequestDTO.builder()
                .vetId("1")
                .schoolName("test school")
                .degree("test degree")
                .fieldOfStudy("test field")
                .startDate("test start year")
                .endDate("test end year")
                .build();
    }

    private Education buildEducation() {
        return Education.builder()
                .id("1")
                .educationId("educationId")
                .vetId("vetId")
                .schoolName("test school")
                .degree("test degree")
                .fieldOfStudy("test field")
                .startDate("test start year")
                .endDate("test end year")
                .build();
    }
    private Vet buildVet() {
        return Vet.builder()
                .id("1")
                .vetId("vetId")
                .vetBillId("1")
                .firstName("Clementine")
                .lastName("LeBlanc")
                .email("skjfhf@gmail.com")
                .phoneNumber("947-238-2847")
                .resume("Just became a vet")
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(false)
                .build();
    }



}