package com.petclinic.products.datalayer.images;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface ImageRepository extends R2dbcRepository<Image, Long> {

    Mono<Image> findImageByImageId(String imageId);
}
