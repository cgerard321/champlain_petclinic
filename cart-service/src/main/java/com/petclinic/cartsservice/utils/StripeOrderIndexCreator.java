package com.petclinic.cartsservice.utils;

import com.petclinic.cartsservice.dataaccesslayer.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

// Stripe can send the same webhook event twice at once.
// This unique index makes sure only one order is ever saved per Stripe session.
// It is created here instead of with @Indexed because Order is also embedded in Cart,
// and @Indexed would add the index to the cart collection too.
@Component
@RequiredArgsConstructor
@Slf4j
public class StripeOrderIndexCreator {

    private final ReactiveMongoTemplate mongoTemplate;

    @PostConstruct
    public void createStripeSessionIndex() {
        mongoTemplate.indexOps(Order.class)
                .ensureIndex(new Index()
                        .on("stripeSessionId", Sort.Direction.ASC)
                        .named("stripeSessionId_unique")
                        .unique()
                        .sparse())
                .subscribe(
                        name -> log.info("Stripe session index ready: {}", name),
                        error -> log.error("Could not create Stripe session index: {}", error.getMessage())
                );
    }
}
