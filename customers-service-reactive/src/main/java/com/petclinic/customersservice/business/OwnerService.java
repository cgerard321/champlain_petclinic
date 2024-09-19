package com.petclinic.customersservice.business;

import com.petclinic.customersservice.data.Owner;
import com.petclinic.customersservice.presentationlayer.OwnerRequestDTO;
import com.petclinic.customersservice.presentationlayer.OwnerResponseDTO;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.data.domain.Pageable;


public interface OwnerService {
    Flux<OwnerResponseDTO> getAllOwners();

    Mono<Long> getTotalNumberOfOwnersWithFilters(String ownerId,String firstName,String lastName,String phoneNumber, String city);

    Flux<OwnerResponseDTO> getAllOwnersPagination(Pageable pageable,
                                                  String ownerId,
                                                  String firstName,
                                                  String lastName,
                                                  String phoneNumber,
                                                  String city);

    Mono<Owner> insertOwner(Mono<Owner> ownerMono);

    // getOwnerByOwnerId is now returning a OwnerResponseDTO
    Mono<OwnerResponseDTO> getOwnerByOwnerId(String ownerId);

//    Mono<Void> deleteOwner(String ownerId);
Mono<OwnerResponseDTO> deleteOwnerByOwnerId(String ownerId);

    Mono<OwnerResponseDTO> updateOwner(Mono<OwnerRequestDTO> ownerRequestDTO, String ownerId);


}
