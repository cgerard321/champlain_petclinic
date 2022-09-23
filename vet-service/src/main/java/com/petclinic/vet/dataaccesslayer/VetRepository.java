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

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface VetRepository extends ReactiveMongoRepository<Vet, String> {

    Mono<Vet> findVetByVetId(String vetId);
    //Mono<Vet> findAllDisabledVets(String vetId);
    //Mono<Vet> findAllEnabledVets(String vetId);
    Mono<Void> deleteVetByVetId (String vetId);

}
