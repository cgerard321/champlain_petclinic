package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VetsServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.RegisterVet;
import com.petclinic.bffapigateway.dtos.Auth.Role;
import com.petclinic.bffapigateway.dtos.Vets.*;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    private VetResponseDTO buildDeactivatedVetResponseDTO(String vetId) {
        Set<Workday> workdaySet = Set.of(Workday.Monday, Workday.Wednesday);

        return VetResponseDTO.builder()
                .vetId(vetId)
                .vetBillId("bill001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .resume("Specialist in dermatology")
                .workday(workdaySet)
                .workHoursJson("{\"08:00-16:00\"}")
                .active(false)
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

    @Test
    void whenGetVets_thenReturnVetsFlux() {
        when(vetsServiceClient.getVets())
                .thenReturn(Flux.just(vetResponseDTO));

        Flux<VetResponseDTO> result = webTestClient.get()
                .uri(BASE_VET_URL)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
                .returnResult(VetResponseDTO.class)
                .getResponseBody();

        StepVerifier.create(result)
                .expectNextMatches(vet -> {
                    assertNotNull(vet);
                    assertEquals(vetResponseDTO.getVetId(), vet.getVetId());
                    assertEquals(vetResponseDTO.getFirstName(), vet.getFirstName());
                    assertEquals(vetResponseDTO.getLastName(), vet.getLastName());
                    return true;
                })
                .verifyComplete();

        verify(vetsServiceClient, times(1)).getVets();
    }

    @Test
    void whenUpdateVet_withValidId_thenReturnUpdatedVet() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";
        when(vetsServiceClient.updateVet(eq(vetId), any(Mono.class)))
                .thenReturn(Mono.just(vetResponseDTO));

        webTestClient.put()
                .uri(BASE_VET_URL + "/" + vetId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(newVetRequestDTO), VetRequestDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(VetResponseDTO.class)
                .value(vet -> {
                    assertNotNull(vet);
                    assertEquals(vetResponseDTO.getVetId(), vet.getVetId());
                    assertEquals(vetResponseDTO.getFirstName(), vet.getFirstName());
                });

        verify(vetsServiceClient, times(1)).updateVet(eq(vetId), any(Mono.class));
    }

    @Test
    void whenUpdateVet_withInvalidId_thenReturnBadRequest() {
        String invalidVetId = "invalid-id";

        webTestClient.put()
                .uri(BASE_VET_URL + "/" + invalidVetId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(newVetRequestDTO), VetRequestDTO.class)
                .exchange()
                .expectStatus().isBadRequest();

        verify(vetsServiceClient, never()).updateVet(anyString(), any(Mono.class));
    }

    @Test
    void whenDeleteVet_withValidId_thenReturnDeactivatedVetResponseDTO() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";

        VetResponseDTO deactivatedVet = buildDeactivatedVetResponseDTO(vetId);


        when(vetsServiceClient.deleteVet(vetId))
                .thenReturn(Mono.just(deactivatedVet));

        webTestClient.delete()
                .uri(BASE_VET_URL + "/" + vetId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(VetResponseDTO.class)
                .value(responseDto -> {
                    assertNotNull(responseDto);
                    assertFalse(responseDto.isActive());
                    assertEquals(vetId, responseDto.getVetId());
                });

        verify(vetsServiceClient, times(1)).deleteVet(vetId);
    }

    @Test
    void whenGetVetByVetId_withValidId_thenReturnVet() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";
        when(vetsServiceClient.getVetByVetId(vetId))
                .thenReturn(Mono.just(vetResponseDTO));

        webTestClient.get()
                .uri(BASE_VET_URL + "/" + vetId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(VetResponseDTO.class)
                .value(vet -> {
                    assertNotNull(vet);
                    assertEquals(vetResponseDTO.getVetId(), vet.getVetId());
                    assertEquals(vetResponseDTO.getFirstName(), vet.getFirstName());
                });

        verify(vetsServiceClient, times(1)).getVetByVetId(vetId);
    }

    @Test
    void whenGetVetByVetId_withValidId_butVetNotFound_thenReturnNotFound() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";
        when(vetsServiceClient.getVetByVetId(vetId))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri(BASE_VET_URL + "/" + vetId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(vetsServiceClient, times(1)).getVetByVetId(vetId);
    }

    @Test
    void whenAddSpecialtiesByVetId_thenReturnUpdatedVet() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";
        SpecialtyDTO specialtyDTO = SpecialtyDTO.builder()
                .specialtyId("cardiology")
                .name("Cardiology")
                .build();

        when(vetsServiceClient.addSpecialtiesByVetId(eq(vetId), any(Mono.class)))
                .thenReturn(Mono.just(vetResponseDTO));

        webTestClient.post()
                .uri(BASE_VET_URL + "/" + vetId + "/specialties")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(specialtyDTO), SpecialtyDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(VetResponseDTO.class)
                .value(vet -> {
                    assertNotNull(vet);
                    assertEquals(vetResponseDTO.getVetId(), vet.getVetId());
                });

        verify(vetsServiceClient, times(1)).addSpecialtiesByVetId(eq(vetId), any(Mono.class));
    }

    @Test
    void whenDeleteSpecialtiesByVetId_thenReturnNoContent() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";
        String specialtyId = "cardiology";

        when(vetsServiceClient.deleteSpecialtiesByVetId(vetId, specialtyId))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(BASE_VET_URL + "/" + vetId + "/specialties/" + specialtyId)
                .exchange()
                .expectStatus().isNoContent();

        verify(vetsServiceClient, times(1)).deleteSpecialtiesByVetId(vetId, specialtyId);
    }

    @Test
    void whenDeleteSpecialtiesByVetId_withNotFound_thenReturnNotFound() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";
        String specialtyId = "invalid-specialty";

        when(vetsServiceClient.deleteSpecialtiesByVetId(vetId, specialtyId))
                .thenReturn(Mono.error(new RuntimeException("Not found")));

        webTestClient.delete()
                .uri(BASE_VET_URL + "/" + vetId + "/specialties/" + specialtyId)
                .exchange()
                .expectStatus().isNotFound();

        verify(vetsServiceClient, times(1)).deleteSpecialtiesByVetId(vetId, specialtyId);
    }

    @Test
    void whenGetPhotoByVetId_thenReturnPhoto() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";
        byte[] photoData = "test photo data".getBytes();
        org.springframework.core.io.Resource photoResource = new org.springframework.core.io.ByteArrayResource(photoData);

        when(vetsServiceClient.getPhotoByVetId(vetId))
                .thenReturn(Mono.just(photoResource));

        webTestClient.get()
                .uri(BASE_VET_URL + "/" + vetId + "/photo")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.IMAGE_JPEG)
                .expectBody(byte[].class)
                .value(body -> {
                    assertNotNull(body);
                    assertArrayEquals(photoData, body);
                });

        verify(vetsServiceClient, times(1)).getPhotoByVetId(vetId);
    }

    @Test
    void whenGetPhotoByVetId_withNotFound_thenReturnNotFound() {
        String vetId = "invalid-vet-id";

        when(vetsServiceClient.getPhotoByVetId(vetId))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri(BASE_VET_URL + "/" + vetId + "/photo")
                .exchange()
                .expectStatus().isNotFound();

        verify(vetsServiceClient, times(1)).getPhotoByVetId(vetId);
    }

    @Test
    void whenUpdatePhotoByVetId_thenReturnUpdatedPhoto() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";
        String photoName = "updated-photo.jpg";
        byte[] photoData = "updated photo data".getBytes();
        org.springframework.core.io.Resource photoResource = new org.springframework.core.io.ByteArrayResource(photoData);

        when(vetsServiceClient.updatePhotoOfVet(eq(vetId), eq(photoName), any(Mono.class)))
                .thenReturn(Mono.just(photoResource));

        webTestClient.put()
                .uri(BASE_VET_URL + "/" + vetId + "/photo/" + photoName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(Mono.just(photoResource), org.springframework.core.io.Resource.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.IMAGE_JPEG)
                .expectBody(byte[].class)
                .value(body -> {
                    assertNotNull(body);
                    assertArrayEquals(photoData, body);
                });

        verify(vetsServiceClient, times(1)).updatePhotoOfVet(eq(vetId), eq(photoName), any(Mono.class));
    }

    @Test
    void whenUpdatePhotoByVetId_withNotFound_thenReturnNotFound() {
        String vetId = "invalid-vet-id";
        String photoName = "photo.jpg";
        byte[] photoData = "photo data".getBytes();
        org.springframework.core.io.Resource photoResource = new org.springframework.core.io.ByteArrayResource(photoData);

        when(vetsServiceClient.updatePhotoOfVet(eq(vetId), eq(photoName), any(Mono.class)))
                .thenReturn(Mono.empty());

        webTestClient.put()
                .uri(BASE_VET_URL + "/" + vetId + "/photo/" + photoName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(Mono.just(photoResource), org.springframework.core.io.Resource.class)
                .exchange()
                .expectStatus().isNotFound();

        verify(vetsServiceClient, times(1)).updatePhotoOfVet(eq(vetId), eq(photoName), any(Mono.class));
    }

    @Test
    void whenAddPhotoByVetId_withOctetStream_thenReturnCreatedPhoto() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";
        String photoName = "new-photo.jpg";
        byte[] photoData = "new photo data".getBytes();
        org.springframework.core.io.Resource photoResource = new org.springframework.core.io.ByteArrayResource(photoData);

        when(vetsServiceClient.addPhotoToVetFromBytes(eq(vetId), eq(photoName), any(byte[].class)))
                .thenReturn(Mono.just(photoResource));

        webTestClient.post()
                .uri(BASE_VET_URL + "/" + vetId + "/photos")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Photo-Name", photoName)
                .body(Mono.just(photoData), byte[].class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .expectBody(byte[].class)
                .value(body -> {
                    assertNotNull(body);
                    assertArrayEquals(photoData, body);
                });

        verify(vetsServiceClient, times(1)).addPhotoToVetFromBytes(eq(vetId), eq(photoName), any(byte[].class));
    }

    @Test
    void whenAddPhotoByVetId_withOctetStream_andError_thenReturnBadRequest() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";
        String photoName = "new-photo.jpg";
        byte[] photoData = "new photo data".getBytes();

        when(vetsServiceClient.addPhotoToVetFromBytes(eq(vetId), eq(photoName), any(byte[].class)))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri(BASE_VET_URL + "/" + vetId + "/photos")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Photo-Name", photoName)
                .body(Mono.just(photoData), byte[].class)
                .exchange()
                .expectStatus().isBadRequest();

        verify(vetsServiceClient, times(1)).addPhotoToVetFromBytes(eq(vetId), eq(photoName), any(byte[].class));
    }

    @Test
    void whenGetAlbumsByVetId_thenReturnAlbums() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";
        Album album1 = new Album(1, vetId, "photo1.jpg", "image/jpeg", "photo1".getBytes());
        Album album2 = new Album(2, vetId, "photo2.jpg", "image/jpeg", "photo2".getBytes());

        when(vetsServiceClient.getAllAlbumsByVetId(vetId))
                .thenReturn(Flux.just(album1, album2));

        webTestClient.get()
                .uri(BASE_VET_URL + "/" + vetId + "/albums")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Album.class)
                .hasSize(2)
                .value(albums -> {
                    assertEquals(2, albums.size());
                    assertEquals(album1.getId(), albums.get(0).getId());
                    assertEquals(album2.getId(), albums.get(1).getId());
                });

        verify(vetsServiceClient, times(1)).getAllAlbumsByVetId(vetId);
    }

    @Test
    void whenDeletePhotoByVetId_thenReturnNoContent() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";

        when(vetsServiceClient.deletePhotoByVetId(vetId))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(BASE_VET_URL + "/" + vetId + "/photo")
                .exchange()
                .expectStatus().isNoContent();

        verify(vetsServiceClient, times(1)).deletePhotoByVetId(vetId);
    }

    @Test
    void whenDeleteAlbumPhoto_thenReturnNoContent() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";
        Integer albumId = 1;

        when(vetsServiceClient.deleteAlbumPhotoById(vetId, albumId))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(BASE_VET_URL + "/" + vetId + "/albums/" + albumId)
                .exchange()
                .expectStatus().isNoContent();

        verify(vetsServiceClient, times(1)).deleteAlbumPhotoById(vetId, albumId);
    }

    @Test
    void whenDeleteAlbumPhoto_withNotFoundException_thenReturnNotFound() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";
        Integer albumId = 999;

        when(vetsServiceClient.deleteAlbumPhotoById(vetId, albumId))
                .thenReturn(Mono.error(new org.webjars.NotFoundException("Album photo not found")));

        webTestClient.delete()
                .uri(BASE_VET_URL + "/" + vetId + "/albums/" + albumId)
                .exchange()
                .expectStatus().isNotFound();

        verify(vetsServiceClient, times(1)).deleteAlbumPhotoById(vetId, albumId);
    }

    @Test
    void whenGetEducationsByVetId_thenReturnEducations() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";

        when(vetsServiceClient.getEducationsByVetId(vetId))
                .thenReturn(Flux.just(educationResponseDTO));

        webTestClient.get()
                .uri(BASE_VET_URL + "/" + vetId + "/educations")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(EducationResponseDTO.class)
                .hasSize(1)
                .value(educations -> {
                    assertEquals(1, educations.size());
                    assertEquals(educationResponseDTO.getEducationId(), educations.get(0).getEducationId());
                });

        verify(vetsServiceClient, times(1)).getEducationsByVetId(vetId);
    }

    @Test
    void whenUpdateEducationByVetIdAndEducationId_thenReturnUpdatedEducation() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";
        String educationId = "edu12345";

        when(vetsServiceClient.updateEducationByVetIdAndByEducationId(eq(vetId), eq(educationId), any(Mono.class)))
                .thenReturn(Mono.just(educationResponseDTO));

        webTestClient.put()
                .uri(BASE_VET_URL + "/" + vetId + "/educations/" + educationId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(educationRequestDTO), EducationRequestDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EducationResponseDTO.class)
                .value(education -> {
                    assertNotNull(education);
                    assertEquals(educationResponseDTO.getEducationId(), education.getEducationId());
                });

        verify(vetsServiceClient, times(1)).updateEducationByVetIdAndByEducationId(eq(vetId), eq(educationId), any(Mono.class));
    }

    @Test
    void whenUpdateEducationByVetIdAndEducationId_withNotFound_thenReturnNotFound() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";
        String educationId = "invalid-education-id";

        when(vetsServiceClient.updateEducationByVetIdAndByEducationId(eq(vetId), eq(educationId), any(Mono.class)))
                .thenReturn(Mono.empty());

        webTestClient.put()
                .uri(BASE_VET_URL + "/" + vetId + "/educations/" + educationId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(educationRequestDTO), EducationRequestDTO.class)
                .exchange()
                .expectStatus().isNotFound();

        verify(vetsServiceClient, times(1)).updateEducationByVetIdAndByEducationId(eq(vetId), eq(educationId), any(Mono.class));
    }

    @Test
    void whenGetRatingsByVetId_thenReturnRatings() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";
        RatingResponseDTO ratingResponseDTO = RatingResponseDTO.builder()
                .ratingId("rating123")
                .vetId(vetId)
                .rateScore(4.5)
                .rateDescription("Excellent service")
                .predefinedDescription(PredefinedDescription.EXCELLENT)
                .customerName("John Doe")
                .rateDate("2023-10-15")
                .build();

        when(vetsServiceClient.getRatingsByVetId(vetId))
                .thenReturn(Flux.just(ratingResponseDTO));

        webTestClient.get()
                .uri(BASE_VET_URL + "/" + vetId + "/ratings")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(RatingResponseDTO.class)
                .hasSize(1)
                .value(ratings -> {
                    assertEquals(1, ratings.size());
                    assertEquals(ratingResponseDTO.getRatingId(), ratings.get(0).getRatingId());
                });

        verify(vetsServiceClient, times(1)).getRatingsByVetId(vetId);
    }

    @Test
    void whenDeleteEducation_thenReturnNoContent() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";
        String educationId = "edu12345";

        when(vetsServiceClient.deleteEducation(vetId, educationId))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(BASE_VET_URL + "/" + vetId + "/educations/" + educationId)
                .exchange()
                .expectStatus().isNoContent();

        verify(vetsServiceClient, times(1)).deleteEducation(vetId, educationId);
    }

    @Test
    void whenAddPhotoByVetIdMultipart_withValidFile_thenReturnCreatedPhoto() {
        // Given
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";
        String photoName = "test-photo.jpg";
        byte[] photoData = "test photo data".getBytes();
        org.springframework.core.io.Resource photoResource = new org.springframework.core.io.ByteArrayResource(photoData);

        when(vetsServiceClient.addPhotoToVet(eq(vetId), eq(photoName), any(FilePart.class)))
                .thenReturn(Mono.just(photoResource));

        webTestClient.post()
                .uri("/api/v2/gateway/vets/{vetId}/photos", vetId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("photoName", photoName)
                        .with("file", new ByteArrayResource(photoData) {
                            @Override
                            public String getFilename() {
                                return photoName;
                            }
                        }))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_OCTET_STREAM);

        verify(vetsServiceClient, times(1)).addPhotoToVet(eq(vetId), eq(photoName), any(FilePart.class));
    }

    @Test
    void whenAddPhotoByVetIdMultipart_withServiceError_thenReturnBadRequest() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";
        String photoName = "test-photo.jpg";
        byte[] photoData = "test photo data".getBytes();

        when(vetsServiceClient.addPhotoToVet(eq(vetId), eq(photoName), any(FilePart.class)))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/api/v2/gateway/vets/{vetId}/photos", vetId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("photoName", photoName)
                        .with("file", new ByteArrayResource(photoData) {
                            @Override
                            public String getFilename() {
                                return photoName;
                            }
                        }))
                .exchange()
                .expectStatus().isBadRequest();

        verify(vetsServiceClient, times(1)).addPhotoToVet(eq(vetId), eq(photoName), any(FilePart.class));
    }

    @Test
    void whenAddAlbumPhotoOctet_withValidData_thenReturnCreatedAlbum() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";
        String photoName = "album-photo.jpg";
        byte[] photoData = "album photo data".getBytes();
        Album expectedAlbum = new Album(1, vetId, photoName, "image/jpeg", photoData);

        when(vetsServiceClient.addAlbumPhotoFromBytes(eq(vetId), eq(photoName), any(byte[].class)))
                .thenReturn(Mono.just(expectedAlbum));

        webTestClient.post()
                .uri("/api/v2/gateway/vets/{vetId}/albums/photos", vetId)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Photo-Name", photoName)
                .bodyValue(photoData)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Album.class)
                .value(album -> {
                    assertEquals(vetId, album.getVetId());
                    assertEquals(photoName, album.getFilename());
                });

        verify(vetsServiceClient, times(1)).addAlbumPhotoFromBytes(eq(vetId), eq(photoName), any(byte[].class));
    }

    @Test
    void whenAddAlbumPhotoOctet_withServiceError_thenReturnBadRequest() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";
        String photoName = "album-photo.jpg";
        byte[] photoData = "album photo data".getBytes();

        when(vetsServiceClient.addAlbumPhotoFromBytes(eq(vetId), eq(photoName), any(byte[].class)))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/api/v2/gateway/vets/{vetId}/albums/photos", vetId)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Photo-Name", photoName)
                .bodyValue(photoData)
                .exchange()
                .expectStatus().isBadRequest();

        verify(vetsServiceClient, times(1)).addAlbumPhotoFromBytes(eq(vetId), eq(photoName), any(byte[].class));
    }

    @Test
    void whenAddAlbumPhotoMultipart_withValidFile_thenReturnCreatedAlbum() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";
        String photoName = "multipart-photo.jpg";
        byte[] photoData = "photo data".getBytes();
        Album expectedAlbum = new Album(2, vetId, photoName, "image/jpeg", photoData);

        when(vetsServiceClient.addAlbumPhoto(eq(vetId), eq(photoName), any(FilePart.class)))
                .thenReturn(Mono.just(expectedAlbum));

        webTestClient.post()
                .uri("/api/v2/gateway/vets/{vetId}/albums/photos", vetId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("photoName", photoName)
                        .with("file", new ByteArrayResource(photoData) {
                            @Override
                            public String getFilename() {
                                return photoName;
                            }
                        }))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Album.class)
                .value(album -> {
                    assertEquals(vetId, album.getVetId());
                    assertEquals(photoName, album.getFilename());
                });

        verify(vetsServiceClient, times(1)).addAlbumPhoto(eq(vetId), eq(photoName), any(FilePart.class));
    }

    @Test
    void whenAddAlbumPhotoMultipart_withServiceError_thenReturnBadRequest() {
        String vetId = "2e26e7a2-8c6e-4e2d-8d60-ad0882e295eb";
        String photoName = "multipart-photo.jpg";
        byte[] photoData = "photo data".getBytes();

        when(vetsServiceClient.addAlbumPhoto(eq(vetId), eq(photoName), any(FilePart.class)))
                .thenReturn(Mono.empty()); 

        webTestClient.post()
                .uri("/api/v2/gateway/vets/{vetId}/albums/photos", vetId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("photoName", photoName)
                        .with("file", new ByteArrayResource(photoData) {
                            @Override
                            public String getFilename() {
                                return photoName;
                            }
                        }))
                .exchange()
                .expectStatus().isBadRequest();

        verify(vetsServiceClient, times(1)).addAlbumPhoto(eq(vetId), eq(photoName), any(FilePart.class));
    }

}