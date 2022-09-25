package com.petclinic.vet.dataaccesslayer;
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

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.transaction.Transactional;

@Repository
public interface VetRepository extends R2dbcRepository<Vet, Integer> {

    @Transactional
    Mono<Vet> findVetByVetId(Integer vetId);

    @Transactional
    Mono<Void> deleteVetByVetId (Integer vetId);

    @Transactional
    Flux<Vet> findVetsByIsActive(boolean isActive);

}
