package com.petclinic.products.datalayer.images;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ImageRepository extends ReactiveMongoRepository<Image, String> {

    Mono<Image> findImageByImageId(String imageId);
}
