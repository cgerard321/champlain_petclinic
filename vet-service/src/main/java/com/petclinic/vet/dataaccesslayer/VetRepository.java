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

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface VetRepository extends ReactiveCrudRepository<Vet, String> {

    Mono<Vet> findVetByVetId(String vetId);

    @Modifying
    @Query(value = "DELETE FROM Vet v WHERE v.vetId=?1")
    Mono<Void> deleteVetByVetId (String vetId);

    Flux<Vet> findVetsByIsActive(boolean isActive);

}
