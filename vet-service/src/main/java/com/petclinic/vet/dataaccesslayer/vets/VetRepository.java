package com.petclinic.vet.dataaccesslayer.vets;
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

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface VetRepository extends ReactiveMongoRepository<Vet, String> {
    Mono<Vet> findVetByVetId(String vetId);

    Mono<Void> deleteVetByVetId (String vetId);

    Flux<Vet> findVetsByActive(boolean isActive);

    Mono<Vet> findVetByVetBillId(String vetBillId);

}
