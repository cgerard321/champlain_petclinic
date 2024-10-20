package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VetsServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VisitsServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.RegisterVet;
import com.petclinic.bffapigateway.dtos.Auth.Role;
import com.petclinic.bffapigateway.dtos.Vets.*;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@WebFluxTest(controllers = VetController.class)
@AutoConfigureWebTestClient
@ContextConfiguration(classes = {
        VetController.class,
        VetsServiceClient.class,
        AuthServiceClient.class
})
class VetControllerUnitTest {
    @Autowired
    private WebTestClient webTestClient;

    @InjectMocks
    private VetController vetController;

    @MockBean
    private VetsServiceClient vetsServiceClient;

    @MockBean
    private AuthServiceClient authServiceClient;

    private final String BASE_VET_URL = "/api/v2/gateway/vets";



    //#region Dummy data
    Set<Workday> workdaySet = Set.of(Workday.Monday,Workday.Wednesday);

    VetRequestDTO newVetRequestDTO = VetRequestDTO.builder()
            .vetId("2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb")
            .vetBillId("bill001")
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .phoneNumber("1234567890")
            .resume("Specialist in dermatology")
            .workday(workdaySet)
            .workHoursJson("{\"08:00-16:00\"}")
            .active(true)
            .photoDefault(false)
            .specialties(Set.of(SpecialtyDTO.builder()
                    .specialtyId("dermatology")
                    .name("Dermatology")
                    .build()))
            .build();

    private VetResponseDTO buildVetResponseDTO() {
        return VetResponseDTO.builder()
                .vetId("2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb")
                .vetBillId("bill001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .resume("Specialist in dermatology")
                .workday(workdaySet)
                .workHoursJson("{\"08:00-16:00\"}")
                .active(true)
                .specialties(Set.of(SpecialtyDTO.builder()
                        .specialtyId("dermatology")
                        .name("Dermatology")
                        .build()))
                .build();
    }
    VetResponseDTO vetResponseDTO = buildVetResponseDTO();

    RegisterVet registerVet = RegisterVet.builder()
            .userId("cb6701ef-22cf-465c-be59-b1ef71cd4f2e")
            .email("john.doe@example.com")
            .username("jad2012")
            .password("Johhhn@1234")
            .vet(newVetRequestDTO)
            .build();

    Role role = Role.builder()
            .id(1)
            .name(Roles.ADMIN.name())
            .build();

    EducationRequestDTO educationRequestDTO = EducationRequestDTO.builder()
            .vetId("2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb")
            .schoolName("Harvard University")
            .degree("Doctor of Veterinary Medicine")
            .fieldOfStudy("Veterinary Science")
            .startDate("2015-08-01")
            .endDate("2019-05-30")
            .build();

    EducationResponseDTO educationResponseDTO = EducationResponseDTO.builder()
            .educationId("edu12345")
            .vetId("2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb")
            .schoolName("Harvard University")
            .degree("Doctor of Veterinary Medicine")
            .fieldOfStudy("Veterinary Science")
            .startDate("2015-08-01")
            .endDate("2019-05-30")
            .build();

    //#endregion


    @Test
    void whenAddVet_asAdmin_thenReturnCreatedVetDTO() {
        VetResponseDTO createdVetResponseDTO = vetResponseDTO;
        when(authServiceClient.addVetUser(any(Mono.class)))
                .thenReturn(Mono.just(createdVetResponseDTO));

        Mono<VetResponseDTO> result = webTestClient.post()
                .uri(BASE_VET_URL + "/users/vets")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(registerVet), RegisterVet.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(VetResponseDTO.class)
                .getResponseBody()
                .single();

        StepVerifier.create(result)
                .expectNextMatches(vetResponseDTO -> {
                    assertNotNull(vetResponseDTO);
                    assertEquals(createdVetResponseDTO.getVetId(), vetResponseDTO.getVetId());
                    assertEquals(createdVetResponseDTO.getFirstName(), vetResponseDTO.getFirstName());
                    assertEquals(createdVetResponseDTO.getLastName(), vetResponseDTO.getLastName());
                    assertEquals(createdVetResponseDTO.getEmail(), vetResponseDTO.getEmail());
                    assertEquals(createdVetResponseDTO.getPhoneNumber(), vetResponseDTO.getPhoneNumber());
                    assertEquals(createdVetResponseDTO.getResume(), vetResponseDTO.getResume());
                    assertEquals(createdVetResponseDTO.getWorkday(), vetResponseDTO.getWorkday());
                    return true;
                })
                .verifyComplete();

        verify(authServiceClient, Mockito.times(1)).addVetUser(any(Mono.class));
    }

    @Test
    void whenAddEducationToVet_asAdmin_thenReturnCreatedEducationDTO() {
        when(vetsServiceClient.addEducationToAVet(anyString(), any(Mono.class)))
                .thenReturn(Mono.just(educationResponseDTO));

        Mono<EducationResponseDTO> result = webTestClient.post()
                .uri(BASE_VET_URL + "/2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb/educations")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(educationRequestDTO), EducationRequestDTO.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .returnResult(EducationResponseDTO.class)
                .getResponseBody()
                .single();

        StepVerifier.create(result)
                .expectNextMatches(education -> {
                    assertNotNull(education);
                    assertEquals(educationResponseDTO.getEducationId(), education.getEducationId());
                    assertEquals(educationResponseDTO.getVetId(), education.getVetId());
                    assertEquals(educationResponseDTO.getSchoolName(), education.getSchoolName());
                    assertEquals(educationResponseDTO.getDegree(), education.getDegree());
                    assertEquals(educationResponseDTO.getFieldOfStudy(), education.getFieldOfStudy());
                    assertEquals(educationResponseDTO.getStartDate(), education.getStartDate());
                    assertEquals(educationResponseDTO.getEndDate(), education.getEndDate());
                    return true;
                })
                .verifyComplete();

        verify(vetsServiceClient, Mockito.times(1)).addEducationToAVet(anyString(), any(Mono.class));
    }

}