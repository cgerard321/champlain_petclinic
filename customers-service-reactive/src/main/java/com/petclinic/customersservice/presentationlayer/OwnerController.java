package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.OwnerService;
import com.petclinic.customersservice.customersExceptions.ApplicationExceptions;
import com.petclinic.customersservice.customersExceptions.exceptions.InvalidInputException;
import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.domainclientlayer.FileRequestDTO;
import com.petclinic.customersservice.util.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/owners")
public class OwnerController {

    private final OwnerService ownerService;

    @GetMapping()
    public Flux<OwnerResponseDTO> getAllOwners() {
        return ownerService.getAllOwners();
    }

    @GetMapping("/owners-count")
    public Mono<ResponseEntity<Long>> getTotalNumberOfOwners(){
        return ownerService.getAllOwners().count()
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }

    @GetMapping("/owners-pagination")
    public Flux<OwnerResponseDTO> getAllOwnersPagination(
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size,
            @RequestParam(required = false) String ownerId,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String city
    ){
        return ownerService.getAllOwnersPagination(
                PageRequest.of(page.orElse(0),size.orElse(5)),ownerId,firstName,lastName,phoneNumber,city);
    }

    @GetMapping("/owners-filtered-count")
    public Mono<Long> getTotalNumberOfOwnersWithFilters(
            @RequestParam(required = false) String ownerId,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String city) {

        return ownerService.getTotalNumberOfOwnersWithFilters(ownerId,firstName,lastName,phoneNumber,city);
    }

    @GetMapping("/{ownerId}")
    public Mono<ResponseEntity<OwnerResponseDTO>> getOwnerByOwnerId(@PathVariable String ownerId, @RequestParam(required = false, defaultValue = "false") boolean includePhoto) {
        return ownerService.getOwnerByOwnerId(ownerId, includePhoto)
                .map(ownerResponseDTO -> ResponseEntity.status(HttpStatus.OK).body(ownerResponseDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping()
    public Mono<ResponseEntity<OwnerResponseDTO>> addOwner(@RequestBody Mono<OwnerRequestDTO> ownerMono) {
        return ownerMono
                .transform(Validator.validateOwner())
                .as(ownerService::addOwner)
                .map(ownerResponseDTO -> ResponseEntity.status(HttpStatus.CREATED).body(ownerResponseDTO));
    }

    @DeleteMapping(value = "/{ownerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<OwnerResponseDTO>> deleteOwnerByOwnerId(@PathVariable String ownerId){
        return Mono.just(ownerId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided course id is invalid: " + ownerId)))
                .flatMap(ownerService::deleteOwnerByOwnerId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PutMapping("/{ownerId}")
    public Mono<ResponseEntity<OwnerResponseDTO>> updateOwner(@RequestBody Mono<OwnerRequestDTO> ownerRequestDTO, @PathVariable String ownerId) {
        return Mono.just(ownerId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(ApplicationExceptions.invalidOwnerId(ownerId))
                .thenReturn(ownerRequestDTO.transform(Validator.validateOwner()))
                .flatMap(request -> ownerService.updateOwner(request, ownerId))
                .map(ResponseEntity::ok)
                .switchIfEmpty(ApplicationExceptions.ownerNotFound(ownerId));
    }

    @PatchMapping("/{ownerId}/photo")
    public Mono<ResponseEntity<OwnerResponseDTO>> updateOwnerPhoto(@PathVariable String ownerId, @RequestBody Mono<FileRequestDTO> photoMono) {
        return Mono.just(ownerId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(ApplicationExceptions.invalidOwnerId(ownerId))
                .flatMap(validId ->
                        photoMono.flatMap(photo ->
                                ownerService.updateOwnerPhoto(validId, photo)
                        )
                )
                .map(ResponseEntity::ok)
                .switchIfEmpty(ApplicationExceptions.ownerNotFound(ownerId));
    }

    @DeleteMapping("/{ownerId}/photo")
    public Mono<ResponseEntity<OwnerResponseDTO>> deleteOwnerPhoto(@PathVariable String ownerId) {
        return ownerService.deleteOwnerPhoto(ownerId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}