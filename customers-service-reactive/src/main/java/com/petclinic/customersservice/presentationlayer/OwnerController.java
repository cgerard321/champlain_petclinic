package com.petclinic.customersservice.presentationlayer;

import com.petclinic.customersservice.business.OwnerService;
import com.petclinic.customersservice.data.Owner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import java.util.Optional;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/owners-pagination")
    public Flux<OwnerResponseDTO> getAllOwnersPagination(
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size
    ){
        return ownerService.getAllOwnersPagination(
                PageRequest.of(page.orElse(0),size.orElse(5)));
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

    @DeleteMapping("/{ownerId}")
    public Mono<Void> deleteOwnerByOwnerId(@PathVariable String ownerId) {
        return ownerService.deleteOwner(ownerId);
    }

    @PutMapping("/{ownerId}")
    public Mono<ResponseEntity<OwnerResponseDTO>> updateOwner(
            @RequestBody Mono<OwnerRequestDTO> ownerRequestDTO,
            @PathVariable String ownerId) {

        return ownerService.updateOwner(ownerRequestDTO, ownerId)
                .map(updatedOwner -> ResponseEntity.ok().body(updatedOwner))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }



}
