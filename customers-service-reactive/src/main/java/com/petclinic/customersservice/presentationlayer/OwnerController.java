package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.OwnerService;
import com.petclinic.customersservice.customersExceptions.exceptions.InvalidInputException;
import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.domainclientlayer.FileResponseDTO;
import com.petclinic.customersservice.domainclientlayer.FilesServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    private final FilesServiceClient filesServiceClient;

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
    public Mono<ResponseEntity<OwnerResponseDTO>> getOwnerByOwnerId(@PathVariable String ownerId) {
        return ownerService.getOwnerByOwnerId(ownerId)
                .map(ownerResponseDTO -> ResponseEntity.status(HttpStatus.OK).body(ownerResponseDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping()
    public Mono<Owner> insertOwner(@RequestBody Mono<Owner> ownerMono) {
        log.info("OwnerController.insertOwner");
        return ownerService.insertOwner(ownerMono);
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
    public Mono<ResponseEntity<OwnerResponseDTO>> updateOwner(
            @RequestBody Mono<OwnerRequestDTO> ownerRequestDTO,
            @PathVariable String ownerId) {

        return ownerService.updateOwner(ownerRequestDTO, ownerId)
                .map(updatedOwner -> ResponseEntity.ok().body(updatedOwner))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{fileId}/photo")
    public Mono<ResponseEntity<FileResponseDTO>> getFileByOwnerId(@PathVariable String fileId) {
        return filesServiceClient.getFile(fileId)
                .map(file -> ResponseEntity.ok().body(file))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
