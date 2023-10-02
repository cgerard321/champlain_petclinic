package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.presentationlayer.OwnerRequestDTO;
import com.petclinic.customersservice.presentationlayer.OwnerResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.data.domain.Pageable;


public interface OwnerService {
    Flux<OwnerResponseDTO> getAllOwners();

    Flux<OwnerResponseDTO> getAllOwnersPagination(Pageable pageable);

    Mono<Owner> insertOwner(Mono<Owner> ownerMono);

    // getOwnerByOwnerId is now returning a OwnerResponseDTO
    Mono<OwnerResponseDTO> getOwnerByOwnerId(String ownerId);

    Mono<Void> deleteOwner(String ownerId);

    Mono<OwnerResponseDTO> updateOwner(Mono<OwnerRequestDTO> ownerRequestDTO, String ownerId);


}
