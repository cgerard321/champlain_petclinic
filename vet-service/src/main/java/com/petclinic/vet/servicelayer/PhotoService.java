package com.petclinic.vet.servicelayer;

import org.springframework.core.io.Resource;
import reactor.core.publisher.Mono;

public interface PhotoService {
    Mono<Resource> getPhotoByVetId(String vetId);
    Mono<Resource> insertPhotoOfVet(String vetId, String photoName, Mono<Resource> photo);
    Mono<Resource> updatePhotoByVetId(String vetId, String photoName, Mono<Resource> photo);
}
