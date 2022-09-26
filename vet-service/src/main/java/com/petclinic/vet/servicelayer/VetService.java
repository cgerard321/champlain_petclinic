package com.petclinic.vet.servicelayer;
/**
 @author Kamilah Hatteea & Brandon Levis : Vet-Service
  * Worked together with (Code with Friends) on IntelliJ IDEA
  * <p>
  * User: @Kamilah Hatteea
  * Date: 2022-09-22
  * Ticket: feat(VVS-CPC-554): edit veterinarian
  * User: Brandon Levis
  * Date: 2022-09-22
  * Ticket: feat(VVS-CPC-553): add veterinarian
 */

import org.springframework.data.jpa.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
public interface VetService {

    Flux<VetDTO> getAll();
    Mono<VetDTO> insertVet(Mono<VetDTO> VetDTOMono);
    Mono<VetDTO> updateVet(Integer vetId, Mono<VetDTO> VetDTOMono);
    Mono<VetDTO> getVetByVetId(Integer vetId);
    Mono<Void> deleteVet(Integer vetId);
    Flux<VetDTO> getVetByIsActive(boolean isActive);
}
