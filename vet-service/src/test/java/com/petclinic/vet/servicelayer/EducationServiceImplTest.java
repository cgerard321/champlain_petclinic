package com.petclinic.vet.servicelayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.vet.dataaccesslayer.education.Education;
import com.petclinic.vet.dataaccesslayer.education.EducationRepository;
import com.petclinic.vet.dataaccesslayer.photos.PhotoRepository;
import com.petclinic.vet.dataaccesslayer.vets.Vet;
import com.petclinic.vet.dataaccesslayer.vets.VetRepository;
import com.petclinic.vet.domainclientlayer.FilesServiceClient;
import com.petclinic.vet.businesslayer.education.EducationService;
import com.petclinic.vet.presentationlayer.education.EducationRequestDTO;
import com.petclinic.vet.presentationlayer.education.EducationResponseDTO;
import com.petclinic.vet.utils.exceptions.NotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.r2dbc.init.R2dbcScriptDatabaseInitializer;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
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
    @MockBean
    FilesServiceClient filesServiceClient;

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
    void deleteEducationByEducationId_Success() {
        when(educationRepository.findByVetIdAndEducationId(anyString(), anyString())).thenReturn(Mono.just(education));
        when(educationRepository.delete(any())).thenReturn(Mono.empty());

        Mono<Void> deletedEducation = educationService.deleteEducationByEducationId(education.getVetId(), education.getEducationId());

        StepVerifier
                .create(deletedEducation)
                .verifyComplete();
    }

    @Test
    void deleteEducationByEducationId_EducationNotFound() {
        when(educationRepository.findByVetIdAndEducationId(anyString(), anyString())).thenReturn(Mono.empty());

        Mono<Void> deletedEducation = educationService.deleteEducationByEducationId(education.getVetId(), education.getEducationId());

        StepVerifier
                .create(deletedEducation)
                .expectError(NotFoundException.class)
                .verify();
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
        // Arrange
        when(vetRepository.findVetByVetId(anyString())).thenReturn(Mono.just(existingVet));
        when(educationRepository.insert((Education) any())).thenReturn(Mono.just(education));

        // Act
        Mono<EducationResponseDTO> educationResponseDTO = educationService.addEducationToVet(Vet_Id, Mono.just(educationRequestDTO));

        // Assert
        StepVerifier.create(educationResponseDTO)
                .consumeNextWith(found -> {
                    assertEquals(education.getEducationId(), found.getEducationId());
                    assertEquals(education.getVetId(), found.getVetId());
                    assertEquals(education.getSchoolName(), found.getSchoolName());
                    assertEquals(education.getDegree(), found.getDegree());
                    assertEquals(education.getFieldOfStudy(), found.getFieldOfStudy());
                    assertEquals(education.getStartDate(), found.getStartDate());
                    assertEquals(education.getEndDate(), found.getEndDate());
                })
                .verifyComplete();
    }

    @Test
    void addEducationToVet_VetNotFound() {
        // Arrange
        when(vetRepository.findVetByVetId(anyString())).thenReturn(Mono.empty());

        // Act
        Mono<EducationResponseDTO> educationResponseDTO = educationService.addEducationToVet(Vet_Id, Mono.just(educationRequestDTO));

        // Assert
        StepVerifier.create(educationResponseDTO)
                .expectError(NotFoundException.class)
                .verify();
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
                .imageId("kjd")
                .workday(new HashSet<>())
                .specialties(new HashSet<>())
                .active(false)
                .build();
    }



}