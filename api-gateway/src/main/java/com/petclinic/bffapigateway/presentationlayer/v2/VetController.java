package com.petclinic.bffapigateway.presentationlayer.v2;

// Test comment for Qodana analysis

import com.petclinic.bffapigateway.domainclientlayer.AuthServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VetsServiceClient;
import com.petclinic.bffapigateway.dtos.Auth.RegisterVet;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Vets.*;
import com.petclinic.bffapigateway.exceptions.ExistingVetNotFoundException;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import com.petclinic.bffapigateway.utils.Security.Annotations.IsUserSpecific;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import com.petclinic.bffapigateway.utils.VetsEntityDtoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.webjars.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.io.IOException;
import java.util.List;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v2/gateway/vets")
@Validated
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:80"})
public class VetController {


    private final VetsServiceClient vetsServiceClient;
    private final AuthServiceClient authServiceClient;



    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VetResponseDTO> getVets(){
        return vetsServiceClient.getVets();
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(value = "/users/vets", consumes = "application/json", produces = "application/json")
    public Mono<ResponseEntity<VetResponseDTO>> addVet( @RequestBody Mono<RegisterVet> registerVetDTO) {
        return authServiceClient.addVetUser(registerVetDTO)
                .map(v -> ResponseEntity.status(HttpStatus.CREATED).body(v))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
   // @IsUserSpecific(idToMatch = {"vetId"}, bypassRoles = {Roles.ADMIN})
    @PutMapping(value = "/{vetId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VetResponseDTO>> updateVet(
            @RequestBody Mono<VetRequestDTO> vetRequestDTOMono,
            @PathVariable String vetId){

        return Mono.just(vetId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided vet Id is invalid" + vetId)))
                .flatMap(id -> vetsServiceClient.updateVet(id, vetRequestDTOMono))
                .map(ResponseEntity::ok)
                .onErrorResume(InvalidInputException.class, e ->
                    Mono.just(ResponseEntity.badRequest().<VetResponseDTO>build()))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "{vetId}")
    public Mono<ResponseEntity<VetResponseDTO>> deleteVet(@PathVariable String vetId) {
        return vetsServiceClient.deleteVet(VetsEntityDtoUtil.verifyId(vetId))
                .map(vetDto -> ResponseEntity.ok().body(vetDto))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping("{vetId}/photo")
    public Mono<ResponseEntity<Resource>> getPhotoByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getPhotoByVetId(vetId)
                .map(r -> ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE).body(r))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @PutMapping("{vetId}/photo/{photoName}")
    public Mono<ResponseEntity<Resource>> updatePhotoByVetId(
            @PathVariable String vetId,
            @PathVariable String photoName,
            @RequestBody Mono<Resource> photo) {

        return vetsServiceClient.updatePhotoOfVet(vetId, photoName, photo)
                .map(r -> ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE).body(r))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
    @PostMapping(value = "{vetId}/photos", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity<Resource>> addPhotoByVetId(
            @PathVariable String vetId,
            @RequestHeader("Photo-Name") String photoName,
            @RequestBody Mono<byte[]> fileData) {

        return fileData.flatMap(bytes -> 
                vetsServiceClient.addPhotoToVetFromBytes(vetId, photoName, bytes)
                        .map(res -> ResponseEntity.status(HttpStatus.CREATED)
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .body(res))
                        .defaultIfEmpty(ResponseEntity.badRequest().build())
        );
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
    @PostMapping(value = "{vetId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<Resource>> addPhotoByVetIdMultipart(
            @PathVariable String vetId,
            @RequestPart("photoName") String photoName,
            @RequestPart("file") FilePart file) {

        return vetsServiceClient.addPhotoToVet(vetId, photoName, file)
                .map(res -> ResponseEntity.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(res))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }
    
    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping(value = "{vetId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VetResponseDTO>> getVetByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getVetByVetId(vetId)
                .map(vet -> ResponseEntity.status(HttpStatus.OK).body(vet))
                .defaultIfEmpty(ResponseEntity.notFound().build());

    }
    //specialty
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET})
    @PostMapping(value = "{vetId}/specialties")
    public Mono<VetResponseDTO> addSpecialtiesByVetId(
            @PathVariable String vetId,
            @RequestBody Mono<SpecialtyDTO> specialties) {
        return vetsServiceClient.addSpecialtiesByVetId(vetId, specialties);
    }
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN,Roles.VET})    
    @DeleteMapping(value = "{vetId}/specialties/{specialtyId}")
    public Mono<ResponseEntity<Void>> deleteSpecialtyByVetId(
            @PathVariable String vetId,
            @PathVariable String specialtyId) {
        return vetsServiceClient.deleteSpecialtyBySpecialtyId(vetId, specialtyId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(RuntimeException.class, e ->
                    Mono.just(ResponseEntity.notFound().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping(
            value = "{vetId}/albums",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<Album> getAllAlbumsByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getAllAlbumsByVetId(vetId)
                .doOnError(error -> log.error("Error fetching photos for vet {}", vetId, error));
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
    @DeleteMapping("{vetId}/photo")
    public Mono<ResponseEntity<Void>> deletePhotoByVetId(@PathVariable String vetId) {
        return vetsServiceClient.deletePhotoByVetId(vetId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .doOnError(error -> log.error("Error deleting photo for vetId: {}", vetId, error));
    }
    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
    @DeleteMapping("/{vetId}/albums/{Id}")
    public Mono<ResponseEntity<Void>> deleteAlbumPhoto(@PathVariable String vetId,@PathVariable Integer Id) {
        return vetsServiceClient.deleteAlbumPhotoById(vetId,Id)
                .then(Mono.defer(() -> Mono.just(ResponseEntity.noContent().<Void>build())))
                .onErrorResume(NotFoundException.class, e -> Mono.defer(() -> Mono.just(ResponseEntity.<Void>notFound().build())));
    }

  @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
  @PostMapping(
    value = "{vetId}/albums/photos",
    consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
public Mono<ResponseEntity<Album>> addAlbumPhotoOctet(
        @PathVariable String vetId,
        @RequestHeader("Photo-Name") String photoName,
        @RequestBody Mono<byte[]> fileData
) {
    return fileData
            .flatMap(bytes -> vetsServiceClient.addAlbumPhotoFromBytes(vetId, photoName, bytes))
            .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved))
            .defaultIfEmpty(ResponseEntity.badRequest().build());
}

@SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
@PostMapping(
    value = "{vetId}/albums/photos",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
public Mono<ResponseEntity<Album>> addAlbumPhotoMultipart(
        @PathVariable String vetId,
        @RequestPart("photoName") String photoName,
        @RequestPart("file") Mono<FilePart> file
) {
    return file
            .flatMap(fp -> vetsServiceClient.addAlbumPhoto(vetId, photoName, fp))
            .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved))
            .defaultIfEmpty(ResponseEntity.badRequest().build());
}


    //education
    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping(
            value = "/{vetId}/educations",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<EducationResponseDTO> getEducationsByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getEducationsByVetId(vetId);
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
    @PostMapping("{vetId}/educations")
    public Mono<ResponseEntity<EducationResponseDTO>> addEducationToVet(
            @PathVariable String vetId,
            @RequestBody Mono<EducationRequestDTO> educationRequestDTOMono) {

        return vetsServiceClient.addEducationToAVet(vetId, educationRequestDTOMono)
                .map(education -> ResponseEntity.status(HttpStatus.CREATED).body(education))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @PutMapping(value = "/{vetId}/educations/{educationId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<EducationResponseDTO>> updateEducationByVetIdAndEducationId(
            @PathVariable String vetId,
            @PathVariable String educationId,
            @RequestBody Mono<EducationRequestDTO> educationRequestDTOMono) {
        return vetsServiceClient.updateEducationByVetIdAndByEducationId(vetId, educationId, educationRequestDTOMono)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping(
            value = "{vetId}/ratings",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<RatingResponseDTO> getRatingsByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getRatingsByVetId(vetId)
                .doOnError(error -> log.error("Error fetching ratings for vet {}", vetId, error));
    }

    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
    @DeleteMapping("/{vetId}/educations/{educationId}")
    public Mono<ResponseEntity<Void>> deleteEducation(
            @PathVariable String vetId,
            @PathVariable String educationId) {
        return vetsServiceClient.deleteEducation(vetId, educationId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
