package com.petclinic.cartsservice.dataaccesslayer;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface PromoRepository extends ReactiveMongoRepository<PromoCode, String> {

     Mono<PromoCode> findPromoCodeById(String promoId);
     Flux<PromoCode> findAllByExpirationDateGreaterThanEqual(LocalDateTime currentDate);
     Mono<PromoCode> findPromoCodeByCode(String promoCode);
}
