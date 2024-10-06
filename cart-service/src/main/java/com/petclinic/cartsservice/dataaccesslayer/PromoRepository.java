package com.petclinic.cartsservice.dataaccesslayer;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface PromoRepository extends ReactiveMongoRepository<PromoCode, String> {

     Mono<PromoCode> findPromoCodeById(String promoId);
}
