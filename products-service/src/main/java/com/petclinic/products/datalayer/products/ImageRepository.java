package com.petclinic.products.datalayer.products;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ImageRepository extends ReactiveMongoRepository<Image, String> {

    Mono<Image> findImageByImageId(String imageId);
}
