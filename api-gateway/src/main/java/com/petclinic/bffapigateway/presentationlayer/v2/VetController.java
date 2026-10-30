package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VetsServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.RegisterVet;
import com.petclinic.bffapigateway.dtos.Files.FileDetails;
import com.petclinic.bffapigateway.dtos.Vets.*;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Annotations.IsUserSpecific;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v2/gateway/vets")
@RequiredArgsConstructor
@Slf4j
public class VetController {

    private final VetsServiceClient vetsServiceClient;
    private final AuthServiceClient authServiceClient;

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(value = "/users/vets", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<VetResponseDTO> addVetUser(@RequestBody Mono<RegisterVet> registerVet, @CookieValue("Bearer") String jwtToken) {
        return authServiceClient.addVetUser(registerVet, jwtToken);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping(produces = {MediaType.TEXT_EVENT_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Flux<VetResponseDTO> getVets() {
        return vetsServiceClient.getVets();
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping(value = "/{vetId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VetResponseDTO>> getVetByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getVet(vetId, false)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
    @IsUserSpecific(idToMatch = {"vetId"}, bypassRoles = {Roles.ADMIN})
    @PutMapping(value = "/{vetId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VetResponseDTO>> updateVet(@PathVariable String vetId, @RequestBody Mono<VetRequestDTO> vetRequestDTO) {
        if (vetId == null || vetId.length() != 36) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        return vetsServiceClient.updateVet(vetId, vetRequestDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "/{vetId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VetResponseDTO>> deleteVet(@PathVariable String vetId) {
        return vetsServiceClient.deleteVet(vetId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(value = "/{vetId}/specialties", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VetResponseDTO>> addSpecialtiesByVetId(@PathVariable String vetId, @RequestBody Mono<SpecialtyDTO> specialtyDTO) {
        return vetsServiceClient.addSpecialtiesByVetId(vetId, specialtyDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "/{vetId}/specialties/{specialtyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteSpecialtyByVetId(@PathVariable String vetId, @PathVariable String specialtyId) {
        return vetsServiceClient.deleteSpecialtyBySpecialtyId(vetId, specialtyId)
                .map(v -> ResponseEntity.noContent().<Void>build())
                .defaultIfEmpty(ResponseEntity.noContent().build())
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping(value = "/{vetId}/photo", produces = MediaType.IMAGE_JPEG_VALUE)
    public Mono<ResponseEntity<Resource>> getPhotoByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getPhotoByVetId(vetId)
                .map(resource -> ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resource))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PutMapping(value = "/{vetId}/photo/{photoName}", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE, produces = MediaType.IMAGE_JPEG_VALUE)
    public Mono<ResponseEntity<Resource>> updatePhotoByVetId(@PathVariable String vetId, @PathVariable String photoName, @RequestBody Mono<Resource> photoResource) {
        return vetsServiceClient.updatePhotoOfVet(vetId, photoName, photoResource)
                .map(resource -> ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resource))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(value = "/{vetId}/photos", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<byte[]>> addPhotoByVetIdOctetStream(@PathVariable String vetId, @RequestHeader("Photo-Name") String photoName, @RequestBody Mono<byte[]> photoData) {
        return photoData.flatMap(data -> {
            FileDetails fileDetails = FileDetails.builder()
                    .fileName(photoName)
                    .fileType("image/jpeg")
                    .fileData(data)
                    .build();
            return vetsServiceClient.updateVetPhoto(vetId, Mono.just(fileDetails))
                    .map(vetResponseDTO -> {
                        if (vetResponseDTO.getPhoto() != null && vetResponseDTO.getPhoto().getFileData() != null) {
                            return ResponseEntity.status(HttpStatus.CREATED)
                                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                    .body(vetResponseDTO.getPhoto().getFileData());
                        }
                        return ResponseEntity.badRequest().<byte[]>build();
                    });
        }).defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(value = "/{vetId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<byte[]>> addPhotoByVetIdMultipart(@PathVariable String vetId, @RequestPart("file") Mono<FilePart> filePartMono, @RequestPart(value = "photoName", required = false) String photoName) {
        return filePartMono.flatMap(filePart -> {
            String name = photoName != null ? photoName : filePart.filename();
            return DataBufferUtils.join(filePart.content())
                    .map(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);
                        return bytes;
                    })
                    .flatMap(bytes -> {
                        FileDetails fileDetails = FileDetails.builder()
                                .fileName(name)
                                .fileType("image/jpeg")
                                .fileData(bytes)
                                .build();
                        return vetsServiceClient.updateVetPhoto(vetId, Mono.just(fileDetails))
                                .map(vetResponseDTO -> {
                                    if (vetResponseDTO.getPhoto() != null && vetResponseDTO.getPhoto().getFileData() != null) {
                                        return ResponseEntity.status(HttpStatus.CREATED)
                                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                                .body(vetResponseDTO.getPhoto().getFileData());
                                    }
                                    return ResponseEntity.badRequest().<byte[]>build();
                                });
                    });
        }).defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping(value = "/{vetId}/albums", produces = {MediaType.TEXT_EVENT_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Flux<Album> getAllAlbumsByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getAllAlbumsByVetId(vetId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "/{vetId}/photo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deletePhotoByVetId(@PathVariable String vetId) {
        return vetsServiceClient.deletePhotoByVetId(vetId)
                .map(v -> ResponseEntity.noContent().<Void>build())
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "/{vetId}/albums/{albumId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteAlbumPhotoById(@PathVariable String vetId, @PathVariable Integer albumId) {
        return vetsServiceClient.deleteAlbumPhotoById(vetId, albumId)
                .map(v -> ResponseEntity.noContent().<Void>build())
                .defaultIfEmpty(ResponseEntity.noContent().build())
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping(value = "/{vetId}/educations", produces = {MediaType.TEXT_EVENT_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Flux<EducationResponseDTO> getEducationsByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getEducationsByVetId(vetId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(value = "/{vetId}/educations", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<EducationResponseDTO>> addEducationToAVet(@PathVariable String vetId, @RequestBody Mono<EducationRequestDTO> educationRequestDTO) {
        return vetsServiceClient.addEducationToAVet(vetId, educationRequestDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PutMapping(value = "/{vetId}/educations/{educationId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<EducationResponseDTO>> updateEducationByVetIdAndEducationId(@PathVariable String vetId, @PathVariable String educationId, @RequestBody Mono<EducationRequestDTO> educationRequestDTO) {
        return vetsServiceClient.updateEducationByVetIdAndByEducationId(vetId, educationId, educationRequestDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "/{vetId}/educations/{educationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteEducation(@PathVariable String vetId, @PathVariable String educationId) {
        return vetsServiceClient.deleteEducation(vetId, educationId)
                .map(v -> ResponseEntity.noContent().<Void>build())
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping(value = "/{vetId}/ratings", produces = {MediaType.TEXT_EVENT_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Flux<RatingResponseDTO> getRatingsByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getRatingsByVetId(vetId);
    }
}