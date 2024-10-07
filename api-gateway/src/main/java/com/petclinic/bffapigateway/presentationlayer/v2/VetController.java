package com.petclinic.bffapigateway.presentationlayer.v2;


import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.domainclientlayer.VetsServiceClient;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerRequestDTO;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Vets.Album;
import com.petclinic.bffapigateway.dtos.Vets.SpecialtyDTO;
import com.petclinic.bffapigateway.dtos.Vets.VetRequestDTO;
import com.petclinic.bffapigateway.dtos.Vets.VetResponseDTO;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v2/gateway/vets")
@Validated
@CrossOrigin(origins = "http://localhost:3000, http://localhost:80")
public class VetController {


    private final VetsServiceClient vetsServiceClient;


    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VetResponseDTO> getVets(){
        return vetsServiceClient.getVets();
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VetResponseDTO>> addVet(@RequestBody Mono<VetRequestDTO> vetRequestDTO){
        return vetsServiceClient.addVet(vetRequestDTO)
                .map(v -> ResponseEntity.status(HttpStatus.CREATED).body(v))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN, Roles.VET})
    //@IsUserSpecific(idToMatch = {"vetId"}, bypassRoles = {Roles.ADMIN})
    @PutMapping(value = "/{vetId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<VetResponseDTO>> updateVet(
            @RequestBody Mono<VetRequestDTO> vetRequestDTOMono,
            @PathVariable String vetId){

        return Mono.just(vetId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided vet Id is invalid " + vetId)))
                .flatMap(id -> vetsServiceClient.updateVet(id, vetRequestDTOMono))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }


    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
    @DeleteMapping(value = "{vetId}")
    public Mono<ResponseEntity<Void>> deleteVet(@PathVariable String vetId) {
        return vetsServiceClient.deleteVet(VetsEntityDtoUtil.verifyId(vetId))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping("{vetId}/photo")
    public Mono<ResponseEntity<Resource>> getPhotoByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getPhotoByVetId(vetId)
                .map(r -> ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE).body(r))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

//    @SecuredEndpoint(allowedRoles = {Roles.ADMIN})
//    @PostMapping(value = "{vetId}/photos/{photoName}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public Mono<ResponseEntity<Resource>> addPhoto(
//            @PathVariable String vetId,
//            @PathVariable String photoName,
//            @RequestParam("image") MultipartFile image) throws IOException {
//
//
//        // Convert MultipartFile to Resource
//        Mono<Resource> resourceMono = Mono.just(new ByteArrayResource(image.getBytes()));
//
//
//        return vetsServiceClient.addPhotoToVet(vetId, photoName, resourceMono)
//                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(r))
//                .defaultIfEmpty(ResponseEntity.badRequest().build());
//    }

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
    public Mono<ResponseEntity<Void>> deleteSpecialtiesByVetId(
            @PathVariable String vetId,
            @PathVariable String specialtyId) {
        return vetsServiceClient.deleteSpecialtiesByVetId(vetId, specialtyId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping(value = "{vetId}/albums", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Album> getAllAlbumsByVetId(@PathVariable String vetId) {
        return vetsServiceClient.getAllAlbumsByVetId(vetId)
                .doOnError(error -> log.error("Error fetching photos for vet {}", vetId, error));
    }


}
