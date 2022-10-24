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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
public interface VetService {

    Flux<VetDTO> getAll();
    Mono<VetDTO> insertVet(Mono<VetDTO> VetDTOMono);
    Mono<VetDTO> updateVet(String vetId, Mono<VetDTO> VetDTOMono);
    Mono<VetDTO> getVetByVetId(String vetId);
    Mono<Void> deleteVetByVetId(String vetId);
    Flux<VetDTO> getVetByIsActive(boolean isActive);
    Mono<VetDTO> getVetByVetBillId(String vetBillId);
}
