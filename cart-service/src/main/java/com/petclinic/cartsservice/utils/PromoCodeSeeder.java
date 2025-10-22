package com.petclinic.cartsservice.utils;

import com.petclinic.cartsservice.dataaccesslayer.PromoCode;
import com.petclinic.cartsservice.dataaccesslayer.PromoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("local")
@Slf4j
public class PromoCodeSeeder {

    private final PromoRepository promoRepository;

    @PostConstruct
    public void seedPromos() {
        promoRepository.count()
                .flatMap(count -> {
                    if (count == 0) {
                        log.info("No promo codes found — seeding defaults...");
                        LocalDateTime exp = LocalDateTime.now().plusMonths(6);

                        List<PromoCode> defaults = List.of(
                                PromoCode.builder()
                                        .Name("Welcome 10%")
                                        .code("WELCOME10")
                                        .discount(10.0)
                                        .expirationDate(exp)
                                        .isActive(true)
                                        .build(),
                                PromoCode.builder()
                                        .Name("Halloween 30%")
                                        .code("HALLOWEEN30")
                                        .discount(30.0)
                                        .expirationDate(exp)
                                        .isActive(true)
                                        .build(),
                                PromoCode.builder()
                                        .Name("Friends 15%")
                                        .code("FRIENDS15")
                                        .discount(15.0)
                                        .expirationDate(exp)
                                        .isActive(true)
                                        .build()
                        );

                        return promoRepository.saveAll(Flux.fromIterable(defaults))
                                .count()
                                .doOnNext(n -> log.info("Inserted {} promo codes.", n))
                                .then(Mono.empty());
                    } else {
                        log.info("Promo codes already exist — skipping seeding ({} existing).", count);
                        return Mono.empty();
                    }
                })
                .subscribe(
                        nil -> {},
                        err -> log.error("Error seeding promo codes: {}", err.getMessage())
                );
    }
}
