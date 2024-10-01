package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.CustomersServiceClient;
import com.petclinic.bffapigateway.dtos.CustomerDTOs.OwnerResponseDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetRequestDTO;
import com.petclinic.bffapigateway.dtos.Pets.PetResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v2/gateway/pet")
@Validated
@CrossOrigin(origins = "http://localhost:3000, http://localhost:80")
public class PetController {

    private final CustomersServiceClient customersServiceClient;

    @PutMapping(value = "/owner/{ownerId}/pets", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @SecuredEndpoint(allowedRoles = {Roles.VET, Roles.ADMIN})
    public Mono<ResponseEntity<List<PetResponseDTO>>> updateOwnerPets(
            @PathVariable("ownerId") String ownerId,
            @RequestBody List<PetRequestDTO> petRequestDTOs) {

        return customersServiceClient.updateOwnerPets(ownerId, petRequestDTOs)
                .map(updatedPets -> ResponseEntity.ok().body(updatedPets))
                .onErrorResume(e -> {
                    log.error("Error updating pets for owner: {}", ownerId, e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }
}

