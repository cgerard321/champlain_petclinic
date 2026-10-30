package com.petclinic.products.datalayer.images;

import org.springframework.data.repository.Repository;
import reactor.core.publisher.Flux;

/** Read-only source retained solely for migration and recovery of original bytes. */
public interface ImageRepository extends Repository<Image, Long> {

    Flux<Image> findAll();
}
