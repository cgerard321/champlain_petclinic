package com.petclinic.inventoryservice.datalayer;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface BundleRepository extends ReactiveMongoRepository<Bundle, String> {

    @Transactional(readOnly = true)
    Mono<Bundle> findByBundleUUID(String bundleUUID);
    Mono<Void>deleteBundleByBundleUUID(String bundleUUID);
    Flux<Bundle> findBundlesByItem(String item);
}
